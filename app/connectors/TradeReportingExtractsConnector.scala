/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import models.availableReports.AvailableReportsViewModel
import config.FrontendAppConfig
import models.report.{ReportConfirmation, ReportRequestUserAnswersModel, RequestedReportsViewModel}
import play.api.Logging

import javax.inject.Singleton
import utils.Constants.eori
import connectors.ConnectorFailureLogger.FromResultToConnectorFailureLogger
import models.thirdparty.{AccountAuthorityOverViewModel, ThirdPartyAddedConfirmation, ThirdPartyRequest}
import models.{AuditDownloadRequest, CompanyInformation, NotificationEmail, ThirdPartyDetails, UserDetails}
import org.apache.pekko.Done
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, NO_CONTENT, OK, TOO_MANY_REQUESTS}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import uk.gov.hmrc.http.HttpReads.Implicits.*

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.writeableOf_JsValue
import play.api.http.Status
import exceptions.NoAuthorisedUserFoundException

@Singleton
class TradeReportingExtractsConnector @Inject() (frontendAppConfig: FrontendAppConfig, httpClient: HttpClientV2)(
  implicit ec: ExecutionContext
) extends Logging {

  def getOrSetupUser(eori: String)(implicit hc: HeaderCarrier): Future[UserDetails] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/eori/setup-user")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori))
      .execute[UserDetails]
      .flatMap:
      response => Future.successful(response)

  def getRequestedReports(eoriNumber: String)(implicit hc: HeaderCarrier): Future[RequestedReportsViewModel] = {
    val requestBody = Json.obj("eori" -> eoriNumber)

    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/requested-reports")
      .setHeader("Content-Type" -> "application/json")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(requestBody)
      .execute[HttpResponse]
      .flatMap {
        case response if response.status == OK =>
          Json
            .parse(response.body)
            .validate[RequestedReportsViewModel]
            .fold(
              errors => {
                val errorMsg = s"JSON validation failed: $errors"
                logger.error(errorMsg)
                Future.failed(new RuntimeException(errorMsg))
              },
              validModel => Future.successful(validModel)
            )

        case response if response.status == NO_CONTENT =>
          logger.info(s"No reports found for EORI: $eoriNumber")
          Future.successful(RequestedReportsViewModel(None, None))

        case response if response.status == BAD_REQUEST =>
          val msg = s"Bad request when fetching reports for EORI: $eoriNumber - ${response.body}"
          logger.warn(msg)
          Future.failed(new IllegalArgumentException(msg))

        case response =>
          val msg = s"Unexpected response ${response.status} from requested-reports API: ${response.body}"
          logger.error(msg)
          Future.failed(new RuntimeException(msg))
      }
      .recover { case ex: Exception =>
        logger.error(s"Exception while fetching requested reports for EORI: $eoriNumber", ex)
        throw ex
      }
  }

  def getAvailableReports(eoriNumber: String)(implicit hc: HeaderCarrier): Future[AvailableReportsViewModel] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/api/available-reports")
      .setHeader("Content-Type" -> "application/json")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj(eori -> eoriNumber))
      .execute[AvailableReportsViewModel]
      .recover { ex =>
        logger.error(s"Failed to fetch Available Reports: ${ex.getMessage}", ex)
        throw ex
      }

  def getNotificationEmail(eori: String)(implicit hc: HeaderCarrier): Future[NotificationEmail] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/user/notification-email")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori))
      .execute[NotificationEmail]
      .recover { ex =>
        logger.error(s"Failed to fetch notification email: ${ex.getMessage}", ex)
        throw ex
      }

  def getCompanyInformation(eori: String)(implicit hc: HeaderCarrier): Future[CompanyInformation] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/company-information")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori))
      .execute[CompanyInformation]
      .recover { ex =>
        logger.error(s"Failed to fetch company information: ${ex.getMessage}", ex)
        throw ex
      }

  def getAuthorisedEoris(eori: String)(implicit hc: HeaderCarrier): Future[Seq[String]] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/user/authorised-eoris")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori))
      .execute[Seq[String]]
      .recover { ex =>
        logger.error(s"Failed to fetch authorised eoris: ${ex.getMessage}", ex)
        throw ex
      }

  def createReportRequest(
    reportRequestAnswers: ReportRequestUserAnswersModel
  )(implicit hc: HeaderCarrier): Future[Seq[ReportConfirmation]] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/create-report-request")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.toJson(reportRequestAnswers))
      .execute[HttpResponse]
      .logFailureReason("Trade reporting extracts connector on createReportRequest")
      .flatMap { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[Seq[ReportConfirmation]] match {
              case JsSuccess(reportConfirmations, _) =>
                Future.successful(reportConfirmations)
              case JsError(errors)                   =>
                logger.error(s"Failed to parse 'report confirmations' from response JSON: $errors")
                Future.failed(
                  UpstreamErrorResponse(
                    "Unexpected response from /trade-reporting-extracts/create-report-request",
                    response.status
                  )
                )
            }
          case _  =>
            logger.error(
              s"Unexpected response from call to /trade-reporting-extracts/create-report-request with status : ${response.status}"
            )
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/create-report-request",
                response.status
              )
            )
        }
      }

  def hasReachedSubmissionLimit(eori: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/report-submission-limit/$eori")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .setHeader("Content-Type" -> "application/json")
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT        => false
          case TOO_MANY_REQUESTS => true
          case _                 =>
            logger.error(
              s"Unexpected response from /trade-reporting-extracts/report-submission-limit: ${response.status}"
            )
            throw new RuntimeException(s"Unexpected response: ${response.status}")
        }
      }

  def getUserDetails(eori: String)(implicit hc: HeaderCarrier): Future[UserDetails] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/eori/get-user-detail")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori))
      .execute[UserDetails]
      .flatMap { response =>
        Future.successful(response)
      }
      .recover { case ex: Exception =>
        logger.error(s"Failed to fetch getUserDetails: ${ex.getMessage}", ex)
        throw ex
      }

  def getThirdPartyDetails(eori: String, thirdPartyEori: String)(implicit
    hc: HeaderCarrier
  ): Future[ThirdPartyDetails] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/third-party-details")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori, "thirdPartyEori" -> thirdPartyEori))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[ThirdPartyDetails] match {
              case JsSuccess(thirdPartyDetails, _) =>
                Future.successful(thirdPartyDetails)
              case JsError(errors)                 =>
                logger.error(s"Failed to parse 'third-party-details' from response JSON: $errors")
                Future.failed(
                  UpstreamErrorResponse(
                    "Unexpected response from /trade-reporting-extracts/third-party-details",
                    response.status
                  )
                )
            }
          case _  =>
            logger.error(s"Failed to fetch third party details: ${response.status} - ${response.body}")
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/third-party-details",
                response.status
              )
            )
        }
      }

  def getAuthorisedBusinessDetails(thirdPartyEori: String, traderEori: String)(implicit
    hc: HeaderCarrier
  ): Future[ThirdPartyDetails] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/authorised-business-details")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("thirdPartyEori" -> thirdPartyEori, "traderEori" -> traderEori))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK        =>
            Json.parse(response.body).validate[ThirdPartyDetails] match {
              case JsSuccess(thirdPartyDetails, _) =>
                Future.successful(thirdPartyDetails)
              case JsError(errors)                 =>
                logger.error(s"Failed to parse 'third-party-details' from response JSON: $errors")
                Future.failed(
                  UpstreamErrorResponse(
                    "Unexpected response from /trade-reporting-extracts/authorised-business-details",
                    response.status
                  )
                )
            }
          case NOT_FOUND =>
            logger.warn(s"No authorised user found for trader EORI - ${response.body}")
            Future.failed(NoAuthorisedUserFoundException(response.body))
          case _         =>
            logger.error(s"Failed to get authorised business details: ${response.status} - ${response.body}")
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/authorised-business-details",
                response.status
              )
            )
        }
      }

  def auditReportDownload(request: AuditDownloadRequest)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/downloaded-audit")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case NO_CONTENT => true
          case _          =>
            logger.error(s"Failed to audit report download: ${response.status} - ${response.body}")
            false
        }
      }

  def getReportRequestLimitNumber(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/report-request-limit-number")
      .execute[String]
      .flatMap { response =>
        Future.successful(response)
      }
      .recover { case ex: Exception =>
        logger.error(s"Failed to fetch report request limit number: ${ex.getMessage}", ex)
        throw ex
      }

  def createThirdPartyAddRequest(
    thirdPartyRequest: ThirdPartyRequest
  )(implicit hc: HeaderCarrier): Future[ThirdPartyAddedConfirmation] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/add-third-party-request")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.toJson(thirdPartyRequest))
      .execute[HttpResponse]
      .logFailureReason("Trade reporting extracts connector on createThirdParyAddRequest")
      .flatMap { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[ThirdPartyAddedConfirmation] match {
              case JsSuccess(thirdPartyAddedConfirmation, _) =>
                Future.successful(thirdPartyAddedConfirmation)
              case JsError(errors)                           =>
                logger.error(s"Failed to parse 'thirdPartyAdded confirmations' from response JSON: $errors")
                Future.failed(
                  UpstreamErrorResponse(
                    "Unexpected response from /trade-reporting-extracts/create-third-party-add-request",
                    response.status
                  )
                )
            }
          case _  =>
            logger.error(
              s"Unexpected response from call to /trade-reporting-extracts/create-third-party-add-request with status : ${response.status}"
            )
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/create-third-party-add-request",
                response.status
              )
            )
        }
      }

  def getAccountsAuthorityOver(eori: String)(implicit hc: HeaderCarrier): Future[Seq[AccountAuthorityOverViewModel]] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/get-users-by-authorised-eori")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("thirdPartyEori" -> eori))
      .execute[Seq[AccountAuthorityOverViewModel]]
      .recover { ex =>
        logger.error(s"Failed to fetch accounts authority over: ${ex.getMessage}", ex)
        throw ex
      }

  def getSelectThirdPartyEori(eori: String)(implicit hc: HeaderCarrier): Future[Seq[AccountAuthorityOverViewModel]] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/get-users-by-authorised-eori-date-filtered")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("thirdPartyEori" -> eori))
      .execute[Seq[AccountAuthorityOverViewModel]]
      .recover { ex =>
        logger.error(s"Failed to fetch accounts authority over: ${ex.getMessage}", ex)
        throw ex
      }

  def selfRemoveThirdPartyAccess(traderEori: String, thirdPartyEori: String)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .delete(url"${frontendAppConfig.tradeReportingExtractsApi}/third-party-access-self-removal")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("traderEori" -> traderEori, "thirdPartyEori" -> thirdPartyEori))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK => Future.successful(Done)
          case _  =>
            logger.error(s"Failed to self-remove third party access: ${response.status} - ${response.body}")
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/third-party-access-self-removals",
                response.status
              )
            )
        }
      }

  def removeThirdParty(eori: String, thirdPartyEori: String)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .delete(url"${frontendAppConfig.tradeReportingExtractsApi}/remove-third-party")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori, "thirdPartyEori" -> thirdPartyEori))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case NO_CONTENT => Future.successful(Done)
          case _          =>
            logger.error(s"Failed to remove third party: ${response.status} - ${response.body}")
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/remove-third-party",
                response.status
              )
            )
        }
      }

  def editThirdPartyRequest(
    thirdPartyRequest: ThirdPartyRequest
  )(implicit hc: HeaderCarrier): Future[ThirdPartyAddedConfirmation] =
    httpClient
      .put(url"${frontendAppConfig.tradeReportingExtractsApi}/edit-third-party-request")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.toJson(thirdPartyRequest))
      .execute[HttpResponse]
      .logFailureReason("Trade reporting extracts connector on editThirdPartyRequest")
      .flatMap { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[ThirdPartyAddedConfirmation] match {
              case JsSuccess(thirdPartyEditedConfirmation, _) =>
                Future.successful(thirdPartyEditedConfirmation)
              case JsError(errors)                            =>
                logger.error(s"Failed to parse 'thirdPartyEditedConfirmation' from response JSON: $errors")
                Future.failed(
                  UpstreamErrorResponse(
                    "Unexpected response from /trade-reporting-extracts/edit-third-party-request",
                    response.status
                  )
                )
            }
          case _  =>
            logger.error(
              s"Unexpected response from call to /trade-reporting-extracts/edit-third-party-request with status : ${response.status}"
            )
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/edit-third-party-request",
                response.status
              )
            )
        }
      }

  def getAdditionalEmails(eori: String)(implicit hc: HeaderCarrier): Future[Seq[String]] =
    httpClient
      .get(url"${frontendAppConfig.tradeReportingExtractsApi}/get-additional-emails")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori))
      .execute[Seq[String]]
      .recover { ex =>
        logger.error(s"Failed to get additional emails: ${ex.getMessage}", ex)
        Seq.empty
      }

  def addAdditionalEmail(eori: String, emailAddress: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/add-additional-email")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori, "emailAddress" -> emailAddress))
      .execute[HttpResponse]
      .map(_.status == 200)
      .recover { ex =>
        logger.error(s"Failed to add additional email: ${ex.getMessage}", ex)
        false
      }

  def removeAdditionalEmail(eori: String, emailAddress: String)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .delete(url"${frontendAppConfig.tradeReportingExtractsApi}/remove-additional-email")
      .setHeader("Authorization" -> s"${frontendAppConfig.internalAuthToken}")
      .withBody(Json.obj("eori" -> eori, "emailAddress" -> emailAddress))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case NO_CONTENT => Future.successful(Done)
          case _          =>
            logger.error(s"Failed to remove additional email ${response.status} - ${response.body}")
            Future.failed(
              UpstreamErrorResponse(
                "Unexpected response from /trade-reporting-extracts/remove-additional-email",
                response.status
              )
            )
        }
      }
}
