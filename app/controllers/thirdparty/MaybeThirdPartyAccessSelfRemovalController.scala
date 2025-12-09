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
import pages.thirdparty.MaybeThirdPartyAccessSelfRemovalPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.MaybeThirdPartyAccessSelfRemovalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MaybeThirdPartyAccessSelfRemovalController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  formProvider: MaybeThirdPartyAccessSelfRemovalFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MaybeThirdPartyAccessSelfRemovalView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(traderEori: String): Action[AnyContent] = (identify andThen getOrCreate) { implicit request =>
    Ok(view(form, traderEori))
  }

  def onSubmit(traderEori: String): Action[AnyContent] = (identify andThen getOrCreate).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, traderEori))),
        value =>
          if (value) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(MaybeThirdPartyAccessSelfRemovalPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.thirdparty.routes.ThirdPartyAccessSelfRemovedController.onPageLoad(traderEori))
          } else {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.remove(MaybeThirdPartyAccessSelfRemovalPage))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.thirdparty.routes.AccountsAuthorityOverController.onPageLoad().url)
          }
      )
  }
}
