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
import forms.thirdparty.ThirdPartyReferenceFormProvider
import models.Mode
import models.thirdparty.AddThirdPartySection
import navigation.ThirdPartyNavigator
import pages.thirdparty.ThirdPartyReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.ThirdPartyReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ThirdPartyReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  thirdPartyNavigator: ThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  addThirdPartySection: AddThirdPartySection,
  requireData: DataRequiredAction,
  formProvider: ThirdPartyReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(ThirdPartyReferencePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ThirdPartyReferencePage, value))
              redirectUrl     = thirdPartyNavigator.nextPage(ThirdPartyReferencePage, mode, updatedAnswers).url
              answersWithNav  = addThirdPartySection.saveNavigation(updatedAnswers, redirectUrl)
              _              <- sessionRepository.set(answersWithNav)
            } yield Redirect(thirdPartyNavigator.nextPage(ThirdPartyReferencePage, mode, answersWithNav))
        )
  }
}
