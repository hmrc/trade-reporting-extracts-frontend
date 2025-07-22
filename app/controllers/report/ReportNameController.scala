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

import controllers.actions.*
import forms.report.ReportNameFormProvider
import models.{Mode, UserAnswers}
import models.report.ReportRequestSection
import navigation.ReportNavigator
import pages.report.ReportNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportHelpers
import views.html.report.ReportNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReportNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  reportRequestSection: ReportRequestSection,
  view: ReportNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def formFor(userAnswers: UserAnswers): Form[String] = {
    val isMultiple                           = ReportHelpers.isMoreThanOneReport(userAnswers)
    val (requiredKey, lengthKey, invalidKey) =
      if (isMultiple) {
        (
          "reportName.error.required.plural",
          "reportName.error.length.plural",
          "reportName.error.invalidCharacters.plural"
        )
      } else {
        ("reportName.error.required", "reportName.error.length", "reportName.error.invalidCharacters")
      }
    formProvider(requiredKey, lengthKey, invalidKey)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form         = formFor(request.userAnswers)
    val preparedForm = request.userAnswers.get(ReportNamePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, ReportHelpers.isMoreThanOneReport(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formFor(request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, ReportHelpers.isMoreThanOneReport(request.userAnswers)))
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ReportNamePage, value))
              redirectUrl     = navigator.nextPage(ReportNamePage, mode, updatedAnswers).url
              answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
              _              <- sessionRepository.set(answersWithNav)
            } yield Redirect(navigator.nextPage(ReportNamePage, mode, answersWithNav))
        )
  }
}
