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
import pages.thirdparty.RemoveThirdPartyPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.RemoveThirdPartyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveThirdPartyController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  sessionRepository: SessionRepository,
  formProvider: RemoveThirdPartyFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveThirdPartyView
)(implicit ec: ExecutionContext)
    extends BaseController {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getOrCreate).async {
    implicit request =>
      Future.successful(Ok(view(form, thirdPartyEori)))
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] = (identify andThen getOrCreate).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, thirdPartyEori))),
        value =>
          if (value) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveThirdPartyPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              controllers.thirdparty.routes.RemoveThirdPartyConfirmationController.onPageLoad(thirdPartyEori)
            )
          } else {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.remove(RemoveThirdPartyPage))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.routes.DashboardController.onPageLoad().url)
          }
      )
  }
}
