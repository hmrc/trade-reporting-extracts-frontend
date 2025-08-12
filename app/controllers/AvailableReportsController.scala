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

package controllers

import controllers.actions.*
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.AvailableReportsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AvailableReportsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: AvailableReportsView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    for {
      availableReports      <- tradeReportingExtractsService.getAvailableReports(request.eori)
      maybeUserReports       = availableReports.availableUserReports.exists(_.nonEmpty)
      maybeThirdPartyReports = availableReports.availableThirdPartyReports.isDefined
    } yield Ok(view(availableReports, maybeUserReports, maybeThirdPartyReports))
  }

  def auditDownloadFile(file: String, fileName: String, reportReference: String): Action[AnyContent] = Action.async {
    implicit request =>
      for {
        downloadResponse <- downloadFile(file, fileName)
      } yield {
        tradeReportingExtractsService.auditReportDownload(reportReference, fileName, file)
        downloadResponse
      }
  }

  def downloadFile(fileUrl: String, fileName: String)(implicit hc: HeaderCarrier): Future[Result] =
    Future.successful(Redirect(fileUrl))
}
