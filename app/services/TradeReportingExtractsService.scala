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
import models.{AuditDownloadRequest, CompanyInformation, NotificationEmail, ThirdPartyDetails, UserDetails}
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TradeReportingExtractsService @Inject() (
)(implicit
  ec: ExecutionContext,
  connector: TradeReportingExtractsConnector
) extends Logging {

  def setupUser(eori: String)(implicit hc: HeaderCarrier): Future[UserDetails] =
    connector.setupUser(eori)

  def getEoriList()(implicit messages: Messages): Future[Seq[SelectItem]] =
    connector.getEoriList().map { eoriStrings =>
      SelectItem(text = messages("accountsYouHaveAuthorityOverImport.defaultValue")) +: eoriStrings.map(eori =>
        SelectItem(text = eori)
      )
    }

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

  def auditReportDownload(
    reportReference: String,
    fileName: String,
    fileUrl: String
  )(implicit hc: HeaderCarrier): Future[Boolean] = {
    val auditData = AuditDownloadRequest(reportReference, fileName, fileUrl)
    connector.auditReportDownload(auditData)
  }
}
