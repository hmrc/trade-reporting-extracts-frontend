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

package controllers.editThirdParty

import controllers.actions.*
import forms.editThirdParty.EditDataEndDateFormProvider
import models.Mode
import models.thirdparty.AddThirdPartySection
import navigation.EditThirdPartyNavigator
import pages.editThirdParty.{EditDataEndDatePage, EditDataStartDatePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.json.OptionalLocalDateReads.*
import views.html.editThirdParty.EditDataEndDateView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditDataEndDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  editThirdPartyNavigator: EditThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EditDataEndDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: EditDataEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService
        .getThirdPartyDetails(request.eori, thirdPartyEori)
        .flatMap { thirdPartyDetails =>

          val startDate = request.userAnswers.get(EditDataStartDatePage(thirdPartyEori)).match {
            case Some(date) => date
            case None       => thirdPartyDetails.dataStartDate.get
          }

          val form         = formProvider(startDate)
          val preparedForm = request.userAnswers.get(EditDataEndDatePage(thirdPartyEori)) match {
            case None  =>
              thirdPartyDetails.dataEndDate match {
                case Some(date) if startDate.isAfter(date) && startDate != date => form
                case Some(date)                                                 => form.fill(Some(date))
                case _                                                          => form
              }
            case value => form.fill(value.get)
          }
          Future.successful(
            Ok(
              view(
                preparedForm,
                thirdPartyEori,
                dateFormatter(startDate)
              )
            )
          )
        }
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap { thirdPartyDetails =>
        val startDate = request.userAnswers.get(EditDataStartDatePage(thirdPartyEori)).match {
          case Some(date) => date
          case None       => thirdPartyDetails.dataStartDate.get
        }
        val form      = formProvider(startDate)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, thirdPartyEori, dateFormatter(startDate)))),
            value =>
              for {
                updatedAnswers <-
                  thirdPartyDetails.dataEndDate match {
                    case Some(originalValue) if Some(originalValue) == value =>
                      Future.fromTry(request.userAnswers.remove(EditDataEndDatePage(thirdPartyEori)))
                    case Some(originalValue) if value.isEmpty                =>
                      // case if user selects 'No End Date' and there was an end date previously, set to LocalDate.MAX to indicate no end date to discern if page was answered
                      Future
                        .fromTry(request.userAnswers.set(EditDataEndDatePage(thirdPartyEori), Some(LocalDate.MAX)))
                    case Some(originalValue)                                 =>
                      Future.fromTry(
                        request.userAnswers.set(EditDataEndDatePage(thirdPartyEori), value)
                      )
                    case None                                                =>
                      Future
                        .fromTry(request.userAnswers.set(EditDataEndDatePage(thirdPartyEori), value))

                  }
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                editThirdPartyNavigator.nextPage(EditDataEndDatePage(thirdPartyEori), userAnswers = updatedAnswers)
              )
          )
      }
  }

  private def dateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val languageTag = if (messages.lang.code == "cy") "cy" else "en"
    date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag(languageTag)))
  }
}
