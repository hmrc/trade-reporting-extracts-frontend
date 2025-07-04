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
import models.requests.IdentifierRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, TradeReportingExtractsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.DashboardView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DashboardController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  auditService: AuditService,
  view: DashboardView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(using ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    audit(request, true)
    tradeReportingExtractsService.setupUser(request.eori).map { userDetails =>
      Ok(view(userDetails))
    }
  }

  def audit(request: IdentifierRequest[AnyContent], isSuccessful: Boolean)(implicit hc: HeaderCarrier): Unit =
    auditService.audit(
      models.audit.UserLoginEvent(
        eori = request.eori,
        userId = request.userId,
        affinityGroup = request.affinityGroup.getClass.getSimpleName,
        credentialRole = request.credentialRole.getOrElse("None").getClass.getSimpleName,
        isSuccessful = isSuccessful
      )
    )
}
