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
import forms.report.CustomRequestStartDateFormProvider
import models.Mode
import navigation.{Navigator, ReportNavigator}
import pages.report.CustomRequestStartDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportHelpers
import views.html.report.CustomRequestStartDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomRequestStartDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  reportNavigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CustomRequestStartDateFormProvider,
  reportHelpers: ReportHelpers,
  val controllerComponents: MessagesControllerComponents,
  view: CustomRequestStartDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val form = formProvider()

    val preparedForm = request.userAnswers.get(CustomRequestStartDatePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, reportHelpers.isMoreThanOneReport(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = formProvider()

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, reportHelpers.isMoreThanOneReport(request.userAnswers)))
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomRequestStartDatePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(reportNavigator.nextPage(CustomRequestStartDatePage, mode, updatedAnswers))
        )
  }
}
