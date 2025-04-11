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

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import views.html.ContactDetailsView

import scala.concurrent.ExecutionContext

class ContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: ContactDetailsView
)(implicit ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = identify async { implicit request =>
    tradeReportingExtractsService.getCompanyInformation().map { companyInformation =>
      Ok(view(companyInformation, "GB123456789002"))
    }
  }
}
