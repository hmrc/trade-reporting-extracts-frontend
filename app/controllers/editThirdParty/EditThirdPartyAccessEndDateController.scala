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
import forms.EditThirdPartyAccessEndDateFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.EditThirdPartyNavigator
import pages.editThirdParty.{EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import utils.DateTimeFormats.dateFormatter
import views.html.editThirdParty.EditThirdPartyAccessEndDateView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditThirdPartyAccessEndDateController @Inject (clock: Clock = Clock.systemUTC())(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: EditThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EditThirdPartyAccessEndDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: EditThirdPartyAccessEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form                  = formProvider(request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)).get)
      val preparedForm          = request.userAnswers.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) match {
        case None  => form
        case value => form.fill(value)
      }
      val dateFormatted: String = getStartDatePlusOneMonth(thirdPartyEori, request)
      Ok(
        view(
          preparedForm,
          dateFormatter(request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)).get),
          thirdPartyEori,
          dateFormatted
        )
      )
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val startDate     = request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)).get
      val form          = formProvider(startDate)
      val dateFormatted = getStartDatePlusOneMonth(thirdPartyEori, request)

      tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap { thirdPartyDetails =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, dateFormatter(startDate), thirdPartyEori, dateFormatted))
              ),
            value => {
              val updatedAnswersF =
                if (startDate == thirdPartyDetails.accessStartDate && value == thirdPartyDetails.accessEndDate) {
                  Future.fromTry(
                    request.userAnswers
                      .remove(EditThirdPartyAccessStartDatePage(thirdPartyEori))
                      .flatMap(_.remove(EditThirdPartyAccessEndDatePage(thirdPartyEori)))
                  )
                } else {
                  value match {
                    case Some(endDate)                                   =>
                      Future.fromTry(request.userAnswers.set(EditThirdPartyAccessEndDatePage(thirdPartyEori), endDate))
                    case None if thirdPartyDetails.accessEndDate.isEmpty =>
                      // case if user selects 'No End Date' and there was no end date previously, user answer not persisted
                      Future.fromTry(request.userAnswers.remove(EditThirdPartyAccessEndDatePage(thirdPartyEori)))
                    case None                                            =>
                      // case if user selects 'No End Date' and there was an end date previously, set to LocalDate.MAX to indicate no end date to discern if page was answered
                      Future.fromTry(
                        request.userAnswers.set(EditThirdPartyAccessEndDatePage(thirdPartyEori), LocalDate.MAX)
                      )
                  }
                }
              updatedAnswersF.flatMap { updatedAnswers =>
                sessionRepository.set(updatedAnswers).map { _ =>
                  Redirect(
                    navigator.nextPage(EditThirdPartyAccessEndDatePage(thirdPartyEori), userAnswers = updatedAnswers)
                  )
                }
              }
            }
          )
      }
    }

  private def getStartDatePlusOneMonth(thirdPartyEori: String, request: DataRequest[AnyContent]): String = {
    val startDate = request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)).get
    val today     = LocalDate.now(clock)
    val baseDate  = if (startDate.isBefore(today)) today else startDate
    baseDate.plusMonths(1).format(DateTimeFormats.dateTimeHintFormat)
  }
}
