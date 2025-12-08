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
import forms.editThirdParty.EditDataStartDateFormProvider
import models.Mode
import models.requests.DataRequest
import models.thirdparty.AddThirdPartySection
import navigation.EditThirdPartyNavigator
import pages.editThirdParty.{EditDataEndDatePage, EditDataStartDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import utils.json.OptionalLocalDateReads.*
import views.html.editThirdParty.EditDataStartDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditDataStartDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  editThirdPartyNavigator: EditThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EditDataStartDateFormProvider,
  addThirdPartySection: AddThirdPartySection,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: EditDataStartDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val currentDate: LocalDate               = LocalDate.now()
  private val currentDateFormatted: String = currentDate.format(DateTimeFormats.dateTimeHintFormat)

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService
        .getThirdPartyDetails(request.eori, thirdPartyEori)
        .flatMap { thirdPartyDetails =>
          val form         = formProvider()
          val preparedForm = request.userAnswers.get(EditDataStartDatePage(thirdPartyEori)) match {
            case None        =>
              thirdPartyDetails.dataStartDate match {
                case Some(date) => form.fill(date)
                case _          => form
              }
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(preparedForm, thirdPartyEori, currentDateFormatted)))
        }
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap { thirdPartyDetails =>
        val form        = formProvider()
        val dataEndDate =
          request.userAnswers.get(EditDataEndDatePage(thirdPartyEori)).getOrElse(thirdPartyDetails.dataEndDate)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, thirdPartyEori, currentDateFormatted))),
            value =>
              dataEndDate match {
                case Some(endDate) if value.isAfter(endDate) && value != endDate =>
                  handleSubmittedValue(request, value, true, thirdPartyDetails.dataStartDate, thirdPartyEori)
                case _                                                           =>
                  handleSubmittedValue(request, value, false, thirdPartyDetails.dataStartDate, thirdPartyEori)
              }
          )
      }
  }

  private def handleSubmittedValue(
    request: DataRequest[AnyContent],
    value: LocalDate,
    clearEndDate: Boolean,
    dataStartDate: Option[LocalDate],
    thirdPartyEori: String
  ) =
    if (clearEndDate) {
      for {
        removeEndDateAnswers <- Future.fromTry(request.userAnswers.remove(EditDataEndDatePage(thirdPartyEori)))
        updatedAnswers       <- dataStartDate.match {
                                  case Some(startDate) if startDate.isEqual(value) =>
                                    Future.fromTry(removeEndDateAnswers.remove(EditDataStartDatePage(thirdPartyEori)))
                                  case _                                           =>
                                    Future.fromTry(removeEndDateAnswers.set(EditDataStartDatePage(thirdPartyEori), value))
                                }
        _                    <- sessionRepository.set(updatedAnswers)
      } yield Redirect(
        editThirdPartyNavigator.nextPage(EditDataStartDatePage(thirdPartyEori), userAnswers = updatedAnswers)
      )
    } else {
      for {
        updatedAnswers <- dataStartDate.match {
                            case Some(startDate) if startDate.isEqual(value) =>
                              Future.fromTry(request.userAnswers.remove(EditDataStartDatePage(thirdPartyEori)))
                            case _                                           =>
                              Future.fromTry(request.userAnswers.set(EditDataStartDatePage(thirdPartyEori), value))
                          }
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(
        editThirdPartyNavigator.nextPage(EditDataStartDatePage(thirdPartyEori), userAnswers = updatedAnswers)
      )
    }
}
