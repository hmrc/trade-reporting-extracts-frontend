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
import forms.thirdparty.ThirdPartyAccessEndDateFormProvider
import models.Mode
import models.thirdparty.AddThirdPartySection
import utils.json.OptionalLocalDateReads.*
import navigation.{Navigator, ThirdPartyNavigator}
import pages.thirdparty.{ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.dateTimeFormat
import views.html.thirdparty.ThirdPartyAccessEndDateView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ThirdPartyAccessEndDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  thirdPartyNavigator: ThirdPartyNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  addThirdPartySection: AddThirdPartySection,
  formProvider: ThirdPartyAccessEndDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyAccessEndDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val form = formProvider(request.userAnswers.get(ThirdPartyAccessStartDatePage).get)

    val preparedForm = request.userAnswers.get(ThirdPartyAccessEndDatePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, dateFormatter(request.userAnswers.get(ThirdPartyAccessStartDatePage).get)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = formProvider(request.userAnswers.get(ThirdPartyAccessStartDatePage).get)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(formWithErrors, mode, dateFormatter(request.userAnswers.get(ThirdPartyAccessStartDatePage).get))
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ThirdPartyAccessEndDatePage, value))
              redirectUrl     = thirdPartyNavigator.nextPage(ThirdPartyAccessEndDatePage, mode, updatedAnswers).url
              answersWithNav  = addThirdPartySection.saveNavigation(updatedAnswers, redirectUrl)
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(thirdPartyNavigator.nextPage(ThirdPartyAccessEndDatePage, mode, updatedAnswers))
        )
  }

  private def dateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val languageTag = if (messages.lang.code == "cy") "cy" else "en"
    date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag(languageTag)))
  }
}
