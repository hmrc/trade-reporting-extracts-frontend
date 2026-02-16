/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.contact

import controllers.actions.*
import forms.additionalEmail.EmailRemovedFormProvider
import pages.additionalEmail.emailRemovedPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contact.emailRemovedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailRemovedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  formProvider: EmailRemovedFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: emailRemovedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(emailAddress: String): Action[AnyContent] = (identify andThen getOrCreate) { implicit request =>

    val preparedForm = request.userAnswers.get(emailRemovedPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, emailAddress))
  }

  def onSubmit(emailAddress: String): Action[AnyContent] = (identify andThen getOrCreate).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, emailAddress))),
        value =>
          if (value) {
            for {
              _ <- tradeReportingExtractsService.removeAddiotnalEmail(request.eori, emailAddress)

              clearedAnswers <- Future.fromTry(request.userAnswers.remove(emailRemovedPage))
              _              <- sessionRepository.set(clearedAnswers)

            } yield Redirect(
              controllers.contact.routes.EmailRemovedConfirmationController.onPageLoad(emailAddress)
            )
          } else {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.remove(emailRemovedPage))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(controllers.contact.routes.ContactDetailsController.onPageLoad())
          }
      )
  }
}
