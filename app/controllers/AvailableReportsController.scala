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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportHelpers
import views.html.AvailableReportsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AvailableReportsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  reportHelpers: ReportHelpers,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: AvailableReportsView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    for {
      availableReports      <- tradeReportingExtractsService.getAvailableReports(request.eori)
      maybeUserReports       = availableReports.availableUserReports.isDefined
      maybeThirdPartyReports = availableReports.availableThirdPartyReports.isDefined
    } yield Ok(view(availableReports, maybeUserReports, maybeThirdPartyReports, reportHelpers))
  }
}
