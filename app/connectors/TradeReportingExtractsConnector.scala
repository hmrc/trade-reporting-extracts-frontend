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

import config.FrontendAppConfig
import models.EoriHistoryResponse
import models.availableReports.AvailableReportsViewModel
import config.FrontendAppConfig
import models.report.ReportRequestUserAnswersModel
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.nio.file.{Files, Paths}
import javax.inject.Singleton
import scala.util.{Failure, Success, Try}
import play.api.libs.ws.writeableOf_JsValue
import utils.Constants.eori
import connectors.ConnectorFailureLogger.FromResultToConnectorFailureLogger
import play.api.http.Status.OK
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import uk.gov.hmrc.http.HttpReads.Implicits.*

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.writeableOf_JsValue

@Singleton
class TradeReportingExtractsConnector @Inject() (frontendAppConfig: FrontendAppConfig, httpClient: HttpClientV2)(
  implicit ec: ExecutionContext
) extends Logging {
  implicit val hc: HeaderCarrier
  private val defaultPath                                                = "conf/resources/eoriList.json"
  // TODO replace with a get request to the backend upon implementation of EORI list
  def getEoriList(pathString: String = defaultPath): Future[Seq[String]] = {
    val path = Paths.get(pathString)

    Try {
      val jsonString = new String(Files.readAllBytes(path), "UTF-8")
      Json.parse(jsonString).as[Seq[String]]
    } match {
      case Success(eoriStrings) =>
        Future.successful(eoriStrings)

      case Failure(ex) =>
        val errMsg = s"Failed to read or parse EORI list from file: ${ex.getMessage}"
        logger.error(errMsg)
        Future.failed(new RuntimeException(errMsg, ex))
    }
  }

  private val reportsPath: String                                                                = "conf/resources/availableReportsData.json"
  // TODO replace with a get request to the backend upon implementation of available reports
  def getAvailableReportsV2(pathString: String = reportsPath): Future[AvailableReportsViewModel] = {
    val path = Paths.get(pathString)

    Try {
      val jsonString = new String(Files.readAllBytes(path), "UTF-8")
      Json.parse(jsonString).as[AvailableReportsViewModel]
    } match {
      case Success(reports) =>
        Future.successful(reports)

      case Failure(ex) =>
        val errMsg = s"Failed to available reports from file: ${ex.getMessage}"
        logger.error(errMsg)
        Future.failed(new RuntimeException(errMsg, ex))
    }
  }

  def getEoriHistory(eoriNumber: String): Future[Option[EoriHistoryResponse]] =
    httpClient
      .get(url"${appConfig.tradeReportingExtractsApi}/eori/eori-history")
      .withBody(Json.obj(eori -> eoriNumber))
      .execute[EoriHistoryResponse]
      .map(response => if (response.eoriHistory.nonEmpty) Some(response) else None)
      .recover { ex =>
        logger.error(s"Failed to fetch EORI history: ${ex.getMessage}", ex)
        throw ex
      }

  def getAvailableReports(eoriNumber: String): Future[AvailableReportsViewModel] =
    httpClient
      .get(url"${appConfig.tradeReportingExtractsApi}/api/available-reports")
      .setHeader("Content-Type" -> "application/json")
      .withBody(Json.obj(eori -> eoriNumber))
      .execute[AvailableReportsViewModel]
      .recover { ex =>
        logger.error(s"Failed to fetch EORI history: ${ex.getMessage}", ex)
        throw ex
      }

  def createReportRequest(
    reportRequestAnswers: ReportRequestUserAnswersModel
  )(implicit hc: HeaderCarrier): Future[Seq[String]] =
    httpClient
      .post(url"${frontendAppConfig.tradeReportingExtractsApi}/create-report-request")
      .withBody(Json.toJson(reportRequestAnswers))
      .execute[HttpResponse]
      .logFailureReason("Trade reporting extracts connector on createReportRequest")
      .flatMap { response =>
        response.status match {
          case OK =>
            val json = Json.parse(response.body)
            (json \ "references").validate[Seq[String]] match {
              case JsSuccess(references, _) => Future.successful(references)
              case JsError(errors)          =>
                logger.error(s"Failed to parse 'references' from response JSON: $errors")
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
}
