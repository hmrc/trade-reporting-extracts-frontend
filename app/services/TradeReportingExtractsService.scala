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

package services

import connectors.TradeReportingExtractsConnector
import models.availableReports.AvailableReportsViewModel
import models.report.{ReportConfirmation, ReportRequestUserAnswersModel, RequestedReportsViewModel}
import models.{AuditDownloadRequest, CompanyInformation, ConsentStatus, NotificationEmail, SelectThirdPartyEori, ThirdPartyDetails, UserDetails}
import org.apache.pekko.Done
import models.thirdparty.{AccountAuthorityOverViewModel, AuthorisedThirdPartiesViewModel, EditThirdPartyRequest, ThirdPartyAddedConfirmation, ThirdPartyRequest}
import org.apache.pekko.Done
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import uk.gov.hmrc.http.HeaderCarrier
import models.UserActiveStatus

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradeReportingExtractsService @Inject() (clock: Clock = Clock.systemUTC())(implicit
  ec: ExecutionContext,
  connector: TradeReportingExtractsConnector
) extends Logging {

  def setupUser(eori: String)(implicit hc: HeaderCarrier): Future[UserDetails] =
    connector.setupUser(eori)

  def getAvailableReports(eori: String)(implicit
    hc: HeaderCarrier
  ): Future[AvailableReportsViewModel] =
    connector.getAvailableReports(eori)

  def getRequestedReports(eori: String)(implicit
    hc: HeaderCarrier
  ): Future[RequestedReportsViewModel] =
    connector.getRequestedReports(eori)

  def getNotificationEmail(eori: String)(implicit
    hc: HeaderCarrier
  ): Future[NotificationEmail] =
    connector.getNotificationEmail(eori)

  def getCompanyInformation(eori: String)(implicit
    hc: HeaderCarrier
  ): Future[CompanyInformation] =
    connector.getCompanyInformation(eori)

  def getAuthorisedEoris(eori: String)(implicit
    hc: HeaderCarrier
  ): Future[Seq[String]] =
    connector.getAuthorisedEoris(eori)

  def createReportRequest(reportRequestAnswers: ReportRequestUserAnswersModel)(implicit
    hc: HeaderCarrier
  ): Future[Seq[ReportConfirmation]] =
    connector.createReportRequest(reportRequestAnswers)

  def hasReachedSubmissionLimit(eori: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    connector.hasReachedSubmissionLimit(eori)

  def getReportRequestLimitNumber(implicit hc: HeaderCarrier): Future[String] =
    connector.getReportRequestLimitNumber

  def getUserDetails(eori: String)(implicit
    hc: HeaderCarrier
  ): Future[UserDetails] =
    connector.getUserDetails(eori)

  def getThirdPartyDetails(eori: String, thirdPartyEori: String)(implicit
    hc: HeaderCarrier
  ): Future[ThirdPartyDetails] =
    connector.getThirdPartyDetails(eori, thirdPartyEori)

  def getAuthorisedBusinessDetails(thirdPartyEori: String, traderEori: String)(implicit
    hc: HeaderCarrier
  ): Future[ThirdPartyDetails] =
    connector.getAuthorisedBusinessDetails(thirdPartyEori, traderEori)

  def selfRemoveThirdPartyAccess(traderEori: String, thirdPartyEori: String)(implicit
    hc: HeaderCarrier
  ): Future[Done] =
    connector.selfRemoveThirdPartyAccess(traderEori, thirdPartyEori)

  def auditReportDownload(
    reportReference: String,
    fileName: String,
    fileUrl: String
  )(implicit hc: HeaderCarrier): Future[Boolean] = {
    val auditData = AuditDownloadRequest(reportReference, fileName, fileUrl)
    connector.auditReportDownload(auditData)
  }

  def createThirdPartyAddRequest(thirdPartyRequest: ThirdPartyRequest)(implicit
    hc: HeaderCarrier
  ): Future[ThirdPartyAddedConfirmation] =
    connector.createThirdPartyAddRequest(thirdPartyRequest)

  def editThirdPartyRequest(thirdPartyRequest: ThirdPartyRequest)(implicit
    hc: HeaderCarrier
  ): Future[ThirdPartyAddedConfirmation] =
    connector.editThirdPartyRequest(thirdPartyRequest)

  def getAuthorisedThirdParties(
    eori: String
  )(implicit hc: HeaderCarrier): Future[Seq[AuthorisedThirdPartiesViewModel]] =
    getUserDetails(eori).flatMap { userDetails =>
      Future.traverse(userDetails.authorisedUsers) { authorisedUser =>
        getCompanyInformation(authorisedUser.eori).map { companyInfo =>
          val businessInfo = if (companyInfo.consent == ConsentStatus.Granted) Some(companyInfo.name) else None
          AuthorisedThirdPartiesViewModel(
            eori = authorisedUser.eori,
            businessInfo = businessInfo,
            referenceName = authorisedUser.referenceName,
            status = UserActiveStatus.fromInstants(
              authorisedUser.accessStart,
              authorisedUser.reportDataStart,
              clock
            )
          )
        }
      }
    }

  def getAccountsAuthorityOver(
    eori: String
  )(implicit hc: HeaderCarrier): Future[Seq[AccountAuthorityOverViewModel]] =
    connector.getAccountsAuthorityOver(eori)

  def removeThirdParty(eori: String, thirdPartyEori: String)(implicit
    hc: HeaderCarrier
  ): Future[Done] =
    connector.removeThirdParty(eori, thirdPartyEori)

  def getSelectThirdPartyEori(eori: String)(implicit hc: HeaderCarrier): Future[SelectThirdPartyEori] =
    connector.getSelectThirdPartyEori(eori).map { accounts =>
      val values  = accounts.map(_.eori)
      val content = accounts.map { acc =>
        acc.businessInfo match {
          case Some(info) => s"${acc.eori} - $info"
          case None       => acc.eori
        }
      }
      SelectThirdPartyEori(content, values)
    }
}
