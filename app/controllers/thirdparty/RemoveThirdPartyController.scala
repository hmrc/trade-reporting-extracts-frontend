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

package controllers.thirdparty

import controllers.BaseController
import controllers.actions.*
import forms.thirdparty.RemoveThirdPartyFormProvider
import models.thirdparty.{ThirdPartyRemovalEvent, ThirdPartyRemovalMeta}
import pages.thirdparty.RemoveThirdPartyPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, TradeReportingExtractsService}
import views.html.thirdparty.RemoveThirdPartyView

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveThirdPartyController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  sessionRepository: SessionRepository,
  formProvider: RemoveThirdPartyFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  auditService: AuditService,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveThirdPartyView,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends BaseController {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] =
    (identify andThen getOrCreate).async { implicit request =>
      Future.successful(Ok(view(form, thirdPartyEori)))
    }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] =
    (identify andThen getOrCreate).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, thirdPartyEori))),
          consentGiven =>
            if (consentGiven) {
              for {
                updatedAnswers    <- Future.fromTry(request.userAnswers.set(RemoveThirdPartyPage, true))
                notificationEmail <- tradeReportingExtractsService.getNotificationEmail(request.eori)
                removalMeta        = ThirdPartyRemovalMeta(
                                       eori = thirdPartyEori,
                                       submittedAt = Instant.now(clock),
                                       notificationEmail = Some(notificationEmail.address)
                                     )

                userAnswersWithMeta = updatedAnswers.copy(submissionMeta = Some(Json.toJson(removalMeta).as[JsObject]))
                _                  <- sessionRepository.set(userAnswersWithMeta)

                _ <- tradeReportingExtractsService.removeThirdParty(request.eori, thirdPartyEori)
                _ <- auditService.auditThirdPartyRemoval(
                       ThirdPartyRemovalEvent(
                         thirdPartyAccessRemovalConsent = true,
                         requesterEori = request.eori,
                         thirdPartyEori = thirdPartyEori
                       )
                     )

                clearedAnswers <- Future.fromTry(userAnswersWithMeta.remove(RemoveThirdPartyPage))
                _              <- sessionRepository.set(clearedAnswers)
              } yield Redirect(
                controllers.thirdparty.routes.RemoveThirdPartyConfirmationController.onPageLoad
              )
            } else {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.remove(RemoveThirdPartyPage))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.routes.DashboardController.onPageLoad())
            }
        )
    }
}
