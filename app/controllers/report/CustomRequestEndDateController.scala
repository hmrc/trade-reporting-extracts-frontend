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

import controllers.*
import controllers.actions.*
import forms.report.CustomRequestEndDateFormProvider
import models.report.ReportRequestSection
import models.{Mode, ThirdPartyDetails}
import navigation.ReportNavigator
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, SelectThirdPartyEoriPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Constants.{maxReportRequestDays, minReportingLagDays}
import utils.DateTimeFormats.dateTimeHintFormat
import utils.{Constants, DateTimeFormats, ErrorHandlers, ReportHelpers}
import views.html.report.CustomRequestEndDateView
import models.AlreadySubmittedFlag
import java.time.temporal.ChronoUnit
import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomRequestEndDateController @Inject (clock: Clock = Clock.systemUTC())(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  reportNavigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CustomRequestEndDateFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  reportRequestSection: ReportRequestSection,
  val controllerComponents: MessagesControllerComponents,
  view: CustomRequestEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(CustomRequestStartDatePage)
        .fold {
          for {
            updatedAnswers                   <-
              Future.successful(ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers))
            updatedAnswersWithSubmissionFlag <- Future.fromTry(updatedAnswers.set(AlreadySubmittedFlag(), true))
            _                                <- sessionRepository.set(updatedAnswersWithSubmissionFlag)
          } yield Redirect(controllers.problem.routes.ReportRequestIssueController.onPageLoad())
        } { startDate =>
          val maybeThirdPartyEori = request.userAnswers.get(SelectThirdPartyEoriPage)
          maybeThirdPartyEori.fold {
            val form         = formProvider(startDate, false, None)
            val preparedForm = request.userAnswers.get(CustomRequestEndDatePage).fold(form)(form.fill)
            Future.successful(
              Ok(
                view(
                  preparedForm,
                  mode,
                  DateTimeFormats.dateFormatter(startDate),
                  calculateMaxEndDate(startDate),
                  ReportHelpers.isMoreThanOneReport(request.userAnswers),
                  maybeThirdPartyRequest = false,
                  None
                )
              )
            )
          } { thirdPartyEori =>
            tradeReportingExtractsService
              .getAuthorisedBusinessDetails(request.eori, thirdPartyEori)
              .map { details =>
                val form         = formProvider(startDate, maybeThirdPartyRequest = true, details.dataEndDate)
                val preparedForm = request.userAnswers.get(CustomRequestEndDatePage).fold(form)(form.fill)
                Ok(
                  view(
                    preparedForm,
                    mode,
                    DateTimeFormats.dateFormatter(startDate),
                    calculateMaxEndDate(startDate),
                    ReportHelpers.isMoreThanOneReport(request.userAnswers),
                    maybeThirdPartyRequest = true,
                    Some(thirdPartyStartEndDateStringGen(startDate, details.dataStartDate, details.dataEndDate))
                  )
                )
              }
              .recoverWith(ErrorHandlers.handleNoAuthorisedUserFoundException(request, sessionRepository))
          }
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybeThirdPartyRequest                                                     = request.userAnswers.get(SelectThirdPartyEoriPage).isDefined
      val startDate: LocalDate                                                       = request.userAnswers.get(CustomRequestStartDatePage).get
      val formAndDetailsFuture: Future[(Form[LocalDate], Option[ThirdPartyDetails])] = if (maybeThirdPartyRequest) {
        tradeReportingExtractsService
          .getAuthorisedBusinessDetails(
            request.eori,
            request.userAnswers.get(SelectThirdPartyEoriPage).get
          )
          .map { details =>
            (formProvider(startDate, maybeThirdPartyRequest, details.dataEndDate), Some(details))
          }
      } else {
        Future.successful((formProvider(startDate, false, None), None))
      }

      formAndDetailsFuture
        .flatMap { case (form, maybeDetails) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                val viewResult = maybeDetails match {
                  case Some(details) =>
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        DateTimeFormats.dateFormatter(startDate), // start date
                        calculateMaxEndDate(startDate), // hint text
                        ReportHelpers.isMoreThanOneReport(request.userAnswers),
                        maybeThirdPartyRequest,
                        Some(thirdPartyStartEndDateStringGen(startDate, details.dataStartDate, details.dataEndDate))
                      )
                    )
                  case None          =>
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        DateTimeFormats.dateFormatter(startDate),
                        calculateMaxEndDate(startDate),
                        ReportHelpers.isMoreThanOneReport(request.userAnswers),
                        maybeThirdPartyRequest,
                        None
                      )
                    )
                }
                Future.successful(viewResult)
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomRequestEndDatePage, value))
                  redirectUrl     = reportNavigator.nextPage(CustomRequestEndDatePage, mode, updatedAnswers).url
                  answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                  _              <- sessionRepository.set(answersWithNav)
                } yield Redirect(reportNavigator.nextPage(CustomRequestEndDatePage, mode, answersWithNav))
            )
        }
        .recoverWith(ErrorHandlers.handleNoAuthorisedUserFoundException(request, sessionRepository))
  }

  private def thirdPartyStartEndDateStringGen(
    startDate: LocalDate,
    dataStartDate: Option[LocalDate],
    dataEndDate: Option[LocalDate]
  )(implicit messages: Messages): String =
    (dataStartDate, dataEndDate) match {
      case (Some(_), Some(_)) =>
        messages("customRequestEndDate.thirdParty.message1", DateTimeFormats.dateFormatter(startDate)) +
          " " + messages("customRequestEndDate.thirdParty.message2")
          + " " + DateTimeFormats.dateFormatter(dataStartDate.get)
          + " " + messages("customRequestEndDate.thirdParty.to")
          + " " + DateTimeFormats.dateFormatter(dataEndDate.get)
          + ". " + messages("customRequestEndDate.thirdParty.message3")
      case (Some(_), _)       =>
        messages("customRequestEndDate.thirdParty.message1", DateTimeFormats.dateFormatter(startDate)) +
          " " + messages("customRequestEndDate.thirdParty.message2")
          + " " + DateTimeFormats.dateFormatter(dataStartDate.get)
          + " " + messages("customRequestEndDate.thirdParty.onwards") + " " + messages(
            "customRequestEndDate.thirdParty.message3"
          )
      case (_, _)             =>
        messages("customRequestEndDate.thirdParty.allDataAccess", DateTimeFormats.dateFormatter(startDate))
    }

  private def calculateMaxEndDate(startDate: LocalDate): String = {
    val ComparisonBufferDays = minReportingLagDays + 1
    val today                = LocalDate.now(clock)
    val startPlusMax         = startDate.plusDays(maxReportRequestDays)
    val baselineEndDate      = startDate.plusDays(maxReportRequestDays - 1)
    val latestAllowedDate    = today.minusDays(ComparisonBufferDays)

    val chosenDate =
      if (startPlusMax.isAfter(today)) {
        latestAllowedDate
      } else {
        val daysDiff = ChronoUnit.DAYS.between(baselineEndDate, today).abs
        if (daysDiff < ComparisonBufferDays) latestAllowedDate
        else baselineEndDate
      }

    chosenDate.format(dateTimeHintFormat)
  }
}
