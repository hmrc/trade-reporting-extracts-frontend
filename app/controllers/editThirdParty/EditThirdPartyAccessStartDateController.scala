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
import forms.EditThirdPartyAccessStartDateFormProvider

import javax.inject.Inject
import navigation.EditThirdPartyNavigator
import pages.editThirdParty.EditThirdPartyAccessStartDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import views.html.editThirdParty.EditThirdPartyAccessStartDateView

import java.time.{Clock, LocalDate}
import scala.concurrent.{ExecutionContext, Future}

class EditThirdPartyAccessStartDateController @Inject (clock: Clock = Clock.systemUTC())(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: EditThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EditThirdPartyAccessStartDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: EditThirdPartyAccessStartDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val currentDate: LocalDate               = LocalDate.now(clock.getZone())
  private val currentDateFormatted: String = currentDate.format(DateTimeFormats.dateTimeHintFormat)

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService
        .getThirdPartyDetails(request.eori, thirdPartyEori)
        .flatMap { thirdPartyDetails =>
          val form         = formProvider()
          val preparedForm = request.userAnswers.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) match {
            case None        =>
              val startDateObjects: LocalDate = thirdPartyDetails.accessStartDate
              form.fill(startDateObjects)
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(preparedForm, thirdPartyEori, currentDateFormatted)))
        }
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap { thirdPartyDetails =>
        val form = formProvider()
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, thirdPartyEori, currentDateFormatted))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(EditThirdPartyAccessStartDatePage(thirdPartyEori), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(EditThirdPartyAccessStartDatePage(thirdPartyEori), userAnswers = updatedAnswers)
              )
          )
      }
  }
}
