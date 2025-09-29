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
import models.{Mode, ThirdPartyDetails}
import models.report.ReportRequestSection
import navigation.ReportNavigator
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, SelectThirdPartyEoriPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
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
  tradeReportingExtractsService: TradeReportingExtractsService,
  reportRequestSection: ReportRequestSection,
  val controllerComponents: MessagesControllerComponents,
  view: CustomRequestEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybeThirdPartyRequest = request.userAnswers.get(SelectThirdPartyEoriPage).isDefined
      val startDate: LocalDate   = request.userAnswers.get(CustomRequestStartDatePage).get

      if (maybeThirdPartyRequest) {
        for {
          details     <- tradeReportingExtractsService.getAuthorisedBusinessDetails(
                           request.eori,
                           request.userAnswers.get(SelectThirdPartyEoriPage).get
                         )
          form         = formProvider(startDate, maybeThirdPartyRequest, details.dataEndDate)
          preparedForm = request.userAnswers.get(CustomRequestEndDatePage) match {
                           case None        => form
                           case Some(value) => form.fill(value)
                         }
        } yield Ok(
          view(
            preparedForm,
            mode,
            reportLengthStringGen(startDate, plus31Days = false),
            reportLengthStringGen(startDate, plus31Days = true),
            ReportHelpers.isMoreThanOneReport(request.userAnswers),
            maybeThirdPartyRequest,
            Some(thirdPartyStartEndDateStringGen(startDate, details.dataStartDate, details.dataEndDate))
          )
        )
      } else {
        val form         = formProvider(startDate, false, None)
        val preparedForm = request.userAnswers.get(CustomRequestEndDatePage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Future.successful(
          Ok(
            view(
              preparedForm,
              mode,
              reportLengthStringGen(startDate, plus31Days = false),
              reportLengthStringGen(startDate, plus31Days = true),
              ReportHelpers.isMoreThanOneReport(request.userAnswers),
              maybeThirdPartyRequest,
              None
            )
          )
        )
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

      formAndDetailsFuture.flatMap { case (form, maybeDetails) =>
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
                      reportLengthStringGen(startDate, plus31Days = false),
                      reportLengthStringGen(startDate, plus31Days = true),
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
                      reportLengthStringGen(startDate, plus31Days = false),
                      reportLengthStringGen(startDate, plus31Days = true),
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
  }

  private def thirdPartyStartEndDateStringGen(
    startDate: LocalDate,
    dataStartDate: Option[LocalDate],
    dataEndDate: Option[LocalDate]
  )(implicit messages: Messages): String = {
    val languageTag = if (messages.lang.code == "cy") "cy" else "en"
    val formatter   = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag(languageTag))
    (dataStartDate, dataEndDate) match {
      case (Some(_), Some(_)) =>
        messages("customRequestEndDate.thirdParty.message1", reportLengthStringGen(startDate, plus31Days = false)) +
          " " + messages("customRequestEndDate.thirdParty.message2")
          + " " + dataStartDate.get.format(formatter)
          + " " + messages("customRequestEndDate.thirdParty.to")
          + " " + dataEndDate.get.format(formatter)
          + ". " + messages("customRequestEndDate.thirdParty.message3")
      case (Some(_), _)       =>
        messages("customRequestEndDate.thirdParty.message1", reportLengthStringGen(startDate, plus31Days = false)) +
          " " + messages("customRequestEndDate.thirdParty.message2")
          + " " + dataStartDate.get.format(formatter)
          + " " + messages("customRequestEndDate.thirdParty.onwards") + " " + messages(
            "customRequestEndDate.thirdParty.message3"
          )
      case (_, _)             =>
        messages("customRequestEndDate.thirdParty.allDataAccess", reportLengthStringGen(startDate, plus31Days = false))
    }
  }

  private def reportLengthStringGen(startDate: LocalDate, plus31Days: Boolean)(implicit messages: Messages): String = {
    val languageTag      = if (messages.lang.code == "cy") "cy" else "en"
    val formatterForHint = DateTimeFormatter.ofPattern("d MM yyyy", Locale.forLanguageTag(languageTag))
    val formatter        = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag(languageTag))
    if (plus31Days) {
      if (startDate.plusDays(31).isAfter(LocalDate.now(ZoneOffset.UTC))) {
        LocalDate.now(ZoneOffset.UTC).minusDays(3).format(formatterForHint)
      } else {
        startDate.plusDays(30).format(formatterForHint)
      }
    } else {
      startDate.format(formatter)
    }
  }
}
