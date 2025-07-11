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
import forms.report.CustomRequestEndDateFormProvider
import models.Mode
import navigation.ReportNavigator
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportHelpers
import views.html.report.CustomRequestEndDateView

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset}
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomRequestEndDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  reportNavigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CustomRequestEndDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CustomRequestEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val startDate: LocalDate = request.userAnswers.get(CustomRequestStartDatePage).get
    val form                 = formProvider(startDate)

    val preparedForm = request.userAnswers.get(CustomRequestEndDatePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(
      view(
        preparedForm,
        mode,
        reportLengthStringGen(startDate, plus31Days = false),
        reportLengthStringGen(startDate, plus31Days = true),
        ReportHelpers.isMoreThanOneReport(request.userAnswers)
      )
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val startDate: LocalDate = request.userAnswers.get(CustomRequestStartDatePage).get
      val form                 = formProvider(startDate)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  reportLengthStringGen(startDate, plus31Days = false),
                  reportLengthStringGen(startDate, plus31Days = true),
                  ReportHelpers.isMoreThanOneReport(request.userAnswers)
                )
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomRequestEndDatePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(reportNavigator.nextPage(CustomRequestEndDatePage, mode, updatedAnswers))
        )
  }

  private def reportLengthStringGen(startDate: LocalDate, plus31Days: Boolean)(implicit messages: Messages): String = {
    val languageTag = if (messages.lang.code == "cy") "cy" else "en"
    val formatter   = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag(languageTag))
    if (plus31Days) {
      if (startDate.plusDays(31).isAfter(LocalDate.now(ZoneOffset.UTC))) {
        LocalDate.now(ZoneOffset.UTC).minusDays(3).format(formatter)
      } else {
        startDate.plusDays(31).format(formatter)
      }
    } else {
      startDate.format(formatter)
    }
  }
}
