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
import forms.report.ReportDateRangeFormProvider
import models.{CheckMode, Mode, UserAnswers}
import models.report.{ReportDateRange, ReportRequestSection}
import navigation.ReportNavigator
import pages.report.ReportDateRangePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DateTimeFormats, ReportHelpers}
import utils.DateTimeFormats.dateTimeFormat
import views.html.report.ReportDateRangeView

import java.time.{Clock, LocalDate, ZoneOffset}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReportDateRangeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReportDateRangeFormProvider,
  reportRequestSection: ReportRequestSection,
  val controllerComponents: MessagesControllerComponents,
  view: ReportDateRangeView,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def formFor(userAnswers: UserAnswers): Form[ReportDateRange] = {
    val moreThanOne = ReportHelpers.isMoreThanOneReport(userAnswers)
    val errorKey    =
      if (moreThanOne) "reportDateRange.pluralReport.error.required" else "reportDateRange.singleReport.error.required"
    formProvider(errorKey)
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form         = formFor(request.userAnswers)
    val preparedForm = request.userAnswers.get(ReportDateRangePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(
      view(preparedForm, mode, lastFullCalendarMonthHintStrings, ReportHelpers.isMoreThanOneReport(request.userAnswers))
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formFor(request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  lastFullCalendarMonthHintStrings,
                  ReportHelpers.isMoreThanOneReport(request.userAnswers)
                )
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ReportDateRangePage, value))
              redirectUrl     = navigator.nextPage(ReportDateRangePage, mode, updatedAnswers).url
              answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
              _              <- sessionRepository.set(answersWithNav)
            } yield Redirect(navigator.nextPage(ReportDateRangePage, mode, answersWithNav))
        )
  }

  private def lastFullCalendarMonthHintStrings(implicit messages: Messages): (String, String) = {
    val startEndDate = DateTimeFormats.lastFullCalendarMonth(LocalDate.now(clock))
    (startEndDate._1.format(dateTimeFormat()(messages.lang)), startEndDate._2.format(dateTimeFormat()(messages.lang)))
  }
}
