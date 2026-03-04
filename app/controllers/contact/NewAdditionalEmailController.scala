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
import forms.additionalEmail.NewAdditionalEmailFormProvider
import pages.additionalEmail.NewAdditionalEmailPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contact.NewAdditionalEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewAdditionalEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: NewAdditionalEmailFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NewAdditionalEmailView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    tradeReportingExtractsService.getAdditionalEmails(request.eori).map { existingEmails =>
      val form         = formProvider(existingEmails)
      val preparedForm = request.userAnswers.flatMap(_.get(NewAdditionalEmailPage)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    tradeReportingExtractsService.getAdditionalEmails(request.eori).flatMap { existingEmails =>
      val form = formProvider(existingEmails)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          value =>
            request.userAnswers match {
              case Some(userAnswers) =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(NewAdditionalEmailPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(controllers.contact.routes.CheckAdditionalEmailController.onPageLoad())
              case None              =>
                for {
                  userAnswers <- Future.fromTry(models.UserAnswers(request.userId).set(NewAdditionalEmailPage, value))
                  _           <- sessionRepository.set(userAnswers)
                } yield Redirect(controllers.contact.routes.CheckAdditionalEmailController.onPageLoad())
            }
        )
    }
  }
}
