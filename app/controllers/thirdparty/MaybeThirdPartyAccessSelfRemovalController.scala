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

import controllers.actions.*
import forms.thirdparty.MaybeThirdPartyAccessSelfRemovalFormProvider
import models.thirdparty.{ThirdPartyRemovalMeta, ThirdPartySelfRemovalEvent}
import pages.thirdparty.MaybeThirdPartyAccessSelfRemovalPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, TradeReportingExtractsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.{dateTimeFormat, formattedSystemTime}
import views.html.thirdparty.MaybeThirdPartyAccessSelfRemovalView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MaybeThirdPartyAccessSelfRemovalController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  formProvider: MaybeThirdPartyAccessSelfRemovalFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  auditService: AuditService,
  val controllerComponents: MessagesControllerComponents,
  view: MaybeThirdPartyAccessSelfRemovalView,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider()
  
  def onPageLoad(traderEori: String): Action[AnyContent] =
    (identify andThen getOrCreate) { implicit request =>
      Ok(view(form, traderEori))
    }

  private def getDateAndTime(implicit messages: Messages): (String, String) =
    (
      LocalDate.now(clock).format(dateTimeFormat()(messages.lang)),
      formattedSystemTime(clock)(messages.lang)
    )

  def onSubmit(traderEori: String): Action[AnyContent] =
    (identify andThen getOrCreate).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, traderEori))),
          value =>
            if (value) {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MaybeThirdPartyAccessSelfRemovalPage, true))
                (date, time)       = getDateAndTime
                removalMeta        = ThirdPartyRemovalMeta(
                  eori = traderEori,
                  submittedDate = date,
                  submittedTime = time,
                  notificationEmail = None
                )
                userAnswersWithMeta = updatedAnswers.copy(submissionMeta = Some(Json.toJson(removalMeta).as[JsObject]))
                _                  <- sessionRepository.set(userAnswersWithMeta)

                _              <- tradeReportingExtractsService.selfRemoveThirdPartyAccess(traderEori, request.eori)
                _              <- auditService.auditThirdPartySelfRemoval(
                                    ThirdPartySelfRemovalEvent(
                                      thirdPartyOwnAccessRemovalConsent = true,
                                      requesterEori = request.eori,
                                      traderEori = traderEori
                                    )
                                  )
                
                clearedAnswers <- Future.fromTry(updatedAnswers.remove(MaybeThirdPartyAccessSelfRemovalPage))
                _              <- sessionRepository.set(clearedAnswers)
              } yield Redirect(
                controllers.thirdparty.routes.ThirdPartyAccessSelfRemovedController.onPageLoad
              )
            } else {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.remove(MaybeThirdPartyAccessSelfRemovalPage))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.thirdparty.routes.AccountsAuthorityOverController.onPageLoad().url)
            }
        )
    }
}
