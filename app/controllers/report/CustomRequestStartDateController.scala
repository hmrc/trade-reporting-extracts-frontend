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
import utils.{DateTimeFormats, ErrorHandlers, ReportHelpers}
import views.html.report.CustomRequestStartDateView

import java.time.LocalDate
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
  tradeReportingExtractsService: TradeReportingExtractsService,
  reportRequestSection: ReportRequestSection,
  val controllerComponents: MessagesControllerComponents,
  view: CustomRequestStartDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val maybeThirdPartyRequest = request.userAnswers.get(SelectThirdPartyEoriPage).isDefined

      if (maybeThirdPartyRequest) {
        (for {
          details     <- tradeReportingExtractsService.getAuthorisedBusinessDetails(
                           request.eori,
                           request.userAnswers.get(SelectThirdPartyEoriPage).get
                         )
          form         = formProvider(maybeThirdPartyRequest, details.dataStartDate, details.dataEndDate)
          preparedForm = request.userAnswers.get(CustomRequestStartDatePage) match {
                           case None        => form
                           case Some(value) => form.fill(value)
                         }
          rangeString  = startEndDateStringGenerator(details.dataStartDate, details.dataEndDate)
        } yield Ok(
          view(
            preparedForm,
            mode,
            ReportHelpers.isMoreThanOneReport(request.userAnswers),
            maybeThirdPartyRequest,
            rangeString
          )
        )).recoverWith(ErrorHandlers.handleNoAuthorisedUserFoundException(request, sessionRepository))
      } else {
        val form         = formProvider(false, None, None)
        val preparedForm = request.userAnswers.get(CustomRequestStartDatePage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Future.successful(
          Ok(
            view(
              preparedForm,
              mode,
              ReportHelpers.isMoreThanOneReport(request.userAnswers),
              maybeThirdPartyRequest,
              None
            )
          )
        )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val maybeThirdPartyRequest = request.userAnswers.get(SelectThirdPartyEoriPage).isDefined

      val formAndDetailsFuture: Future[(Form[LocalDate], Option[ThirdPartyDetails])] = if (maybeThirdPartyRequest) {
        tradeReportingExtractsService
          .getAuthorisedBusinessDetails(
            request.eori,
            request.userAnswers.get(SelectThirdPartyEoriPage).get
          )
          .map { details =>
            (formProvider(maybeThirdPartyRequest = true, details.dataStartDate, details.dataEndDate), Some(details))
          }
      } else {
        Future.successful((formProvider(maybeThirdPartyRequest = false, None, None), None))
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
                        ReportHelpers.isMoreThanOneReport(request.userAnswers),
                        maybeThirdPartyRequest,
                        startEndDateStringGenerator(details.dataStartDate, details.dataEndDate)
                      )
                    )
                  case None          =>
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        ReportHelpers.isMoreThanOneReport(request.userAnswers),
                        maybeThirdPartyRequest,
                        None
                      )
                    )
                }
                Future.successful(viewResult)
              },
              value =>
                (
                  request.userAnswers.get(CustomRequestStartDatePage),
                  request.userAnswers.get(CustomRequestEndDatePage)
                ) match {
                  case (Some(startDate), Some(endDate)) if value.isBefore(startDate) || value.isAfter(endDate) =>
                    for {
                      removedEndDate <- Future.fromTry(request.userAnswers.remove(CustomRequestEndDatePage))
                      updatedAnswers <- Future.fromTry(removedEndDate.set(CustomRequestStartDatePage, value))
                      redirectUrl     = reportNavigator.nextPage(CustomRequestStartDatePage, mode, updatedAnswers).url
                      answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                      _              <- sessionRepository.set(answersWithNav)
                    } yield Redirect(redirectUrl)
                  case _                                                                                       =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(CustomRequestStartDatePage, value))
                      redirectUrl     = reportNavigator.nextPage(CustomRequestStartDatePage, mode, updatedAnswers).url
                      answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                      _              <- sessionRepository.set(answersWithNav)
                    } yield Redirect(redirectUrl)
                }
            )
        }
        .recoverWith(ErrorHandlers.handleNoAuthorisedUserFoundException(request, sessionRepository))
    }

  private def startEndDateStringGenerator(startDate: Option[LocalDate], endDate: Option[LocalDate])(implicit
    messages: Messages
  ): Option[String] =
    (startDate, endDate) match {
      case (Some(_), None)    =>
        Some(
          messages("customRequestStartDate.message2.thirdParty")
            + " " + DateTimeFormats.dateFormatter(startDate.get) + " " + messages(
              "customRequestStartDate.thirdParty.onwards"
            )
        )
      case (Some(_), Some(_)) =>
        Some(
          messages("customRequestStartDate.message2.thirdParty")
            + " " + DateTimeFormats.dateFormatter(startDate.get) + " " + messages(
              "customRequestStartDate.thirdParty.to"
            ) + " " + DateTimeFormats.dateFormatter(endDate.get) + "."
        )
      case (_, _)             => None
    }
}
