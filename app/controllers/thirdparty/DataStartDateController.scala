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

package controllers.thirdparty

import controllers.actions.*
import forms.thirdparty.DataStartDateFormProvider
import models.Mode
import models.requests.DataRequest
import models.thirdparty.AddThirdPartySection
import navigation.ThirdPartyNavigator
import pages.thirdparty.{DataEndDatePage, DataStartDatePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import utils.json.OptionalLocalDateReads.*
import views.html.thirdparty.DataStartDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataStartDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  thirdPartyNavigator: ThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DataStartDateFormProvider,
  addThirdPartySection: AddThirdPartySection,
  val controllerComponents: MessagesControllerComponents,
  view: DataStartDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val currentDate: LocalDate               = LocalDate.now()
  private val currentDateFormatted: String = currentDate.format(DateTimeFormats.dateTimeHintFormat)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val form = formProvider()

    val preparedForm = request.userAnswers.get(DataStartDatePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, currentDateFormatted))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = formProvider()

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, currentDateFormatted))),
          value =>
            request.userAnswers.get(DataEndDatePage) match {
              case Some(Some(endDate)) if value.isAfter(endDate) && value != endDate =>
                handleSubmittedValue(mode, request, value, true)
              case _                                                                 =>
                handleSubmittedValue(mode, request, value, false)
            }
        )
  }

  private def handleSubmittedValue(
    mode: Mode,
    request: DataRequest[AnyContent],
    value: LocalDate,
    clearEndDate: Boolean
  ) =
    if (clearEndDate) {
      for {
        removeEndDateAnswers <- Future.fromTry(request.userAnswers.remove(DataEndDatePage))
        updatedAnswers       <- Future.fromTry(removeEndDateAnswers.set(DataStartDatePage, value))
        redirectUrl           = thirdPartyNavigator.nextPage(DataStartDatePage, mode, updatedAnswers).url
        answersWithNav        = addThirdPartySection.saveNavigation(updatedAnswers, redirectUrl)
        _                    <- sessionRepository.set(updatedAnswers)
      } yield Redirect(thirdPartyNavigator.nextPage(DataStartDatePage, mode, answersWithNav))
    } else {
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(DataStartDatePage, value))
        redirectUrl     = thirdPartyNavigator.nextPage(DataStartDatePage, mode, updatedAnswers).url
        answersWithNav  = addThirdPartySection.saveNavigation(updatedAnswers, redirectUrl)
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(thirdPartyNavigator.nextPage(DataStartDatePage, mode, answersWithNav))
    }
}
