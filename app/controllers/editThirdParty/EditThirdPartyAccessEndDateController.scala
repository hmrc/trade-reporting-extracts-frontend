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
import forms.editThirdParty.EditThirdPartyAccessEndDateFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.EditThirdPartyNavigator
import pages.editThirdParty.{EditDeclarationDatePage, EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage}
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

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) match {
        case None            =>
          val updatedAnswers = request.userAnswers.remove(EditThirdPartyAccessEndDatePage(thirdPartyEori)).get
          sessionRepository.set(updatedAnswers).map { _ =>
            Redirect(controllers.problem.routes.EditThirdPartyGeneralProblemController.onPageLoad())
          }
        case Some(startDate) =>
          val form = formProvider(startDate)

          val preparedForm = request.userAnswers.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) match {
            case Some(d) if d == LocalDate.MAX => form
            case Some(d)                       => form.fill(Some(d))
            case None                          => form
          }

          val dateFormatted: String = getStartDatePlusOneMonth(thirdPartyEori, startDate)
          Future.successful(
            Ok(
              view(
                preparedForm,
                dateFormatter(startDate),
                thirdPartyEori,
                dateFormatted
              )
            )
          )
      }
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) match {
        case Some(startDate) =>
          tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap {
            thirdPartyDetails =>

              val form          = formProvider(startDate)
              val dateFormatted = getStartDatePlusOneMonth(thirdPartyEori, startDate)

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
                            Future.fromTry(
                              request.userAnswers.set(EditThirdPartyAccessEndDatePage(thirdPartyEori), endDate)
                            )
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
                          navigator
                            .nextPage(EditThirdPartyAccessEndDatePage(thirdPartyEori), userAnswers = updatedAnswers)
                        )
                      }
                    }
                  }
                )
          }
        case None            =>
          val updatedAnswers = request.userAnswers.remove(EditThirdPartyAccessEndDatePage(thirdPartyEori)).get
          sessionRepository.set(updatedAnswers).map { _ =>
            Redirect(controllers.problem.routes.EditThirdPartyGeneralProblemController.onPageLoad())
          }
      }
    }

  private def getStartDatePlusOneMonth(thirdPartyEori: String, startDate: LocalDate): String = {
    val today    = LocalDate.now(clock)
    val baseDate = if (startDate.isBefore(today)) today else startDate
    baseDate.plusMonths(1).format(DateTimeFormats.dateTimeHintFormat)
  }
}
