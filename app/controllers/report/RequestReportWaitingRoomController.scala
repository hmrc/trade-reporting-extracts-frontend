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

package controllers.report

import controllers.BaseController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.MessagesApi
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ReportRequestDataService, TradeReportingExtractsService}
import views.html.report.RequestReportWaitingRoomView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestReportWaitingRoomController @Inject() (
                                                     override val messagesApi: MessagesApi,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     sessionRepository: SessionRepository,
                                                     tradeReportingExtractsService: TradeReportingExtractsService,
                                                     view: RequestReportWaitingRoomView,
                                                     reportRequestDataService: ReportRequestDataService,
                                                     val controllerComponents: MessagesControllerComponents) (implicit ec: ExecutionContext) extends BaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      requestRefs    <- tradeReportingExtractsService.createReportRequest(
        reportRequestDataService.buildReportRequest(request.userAnswers, request.eori)
      )
      requestRef      = requestRefs.mkString(", ")
      updatedAnswers <- Future.fromTry(request.userAnswers.removePath(JsPath \ "report"))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(controllers.report.routes.RequestConfirmationController.onPageLoad(requestRef))
  }
}