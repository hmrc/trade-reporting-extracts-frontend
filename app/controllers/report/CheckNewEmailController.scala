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
import forms.report.CheckNewEmailFormProvider
import models.Mode
import models.report.ReportRequestSection
import models.requests.DataRequest
import navigation.{Navigator, ReportNavigator}
import pages.report.{CheckNewEmailPage, EmailSelectionPage, NewEmailNotificationPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.report.CheckNewEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckNewEmailController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         reportNavigator: ReportNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: CheckNewEmailFormProvider,
                                         reportRequestSection: ReportRequestSection,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: CheckNewEmailView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(CheckNewEmailPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getLastEnteredEmail(request)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, getLastEnteredEmail(request)))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CheckNewEmailPage, value))
            redirectUrl     = reportNavigator.nextPage(CheckNewEmailPage, mode, updatedAnswers).url
            answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
            _              <- sessionRepository.set(answersWithNav)
          } yield Redirect(reportNavigator.nextPage(CheckNewEmailPage, mode, answersWithNav))
      )
  }

  private def getLastEnteredEmail(request: DataRequest[AnyContent])(implicit messages: Messages): String ={
    val emailSelectionAnswer: Option[String] = request.userAnswers.get(NewEmailNotificationPage)
    emailSelectionAnswer.getOrElse(messages("error.prefix"))
  }
}
