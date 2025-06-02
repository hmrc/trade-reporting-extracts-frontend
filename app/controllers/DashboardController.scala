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
import models.AllowedEoris

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.DashboardView

import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: DashboardView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(using ec: ExecutionContext)
    extends BaseController
    with AllowedEoris {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    if (!allowedEoris.contains(request.eori)) {
      Future.successful(Redirect(controllers.problem.routes.UnauthorisedController.onPageLoad()))
    } else {
      tradeReportingExtractsService.setupUser(request.eori).map { userDetails =>
        Ok(view(userDetails))
      }
    }
  }
}
