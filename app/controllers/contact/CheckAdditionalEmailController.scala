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
import forms.additionalEmail.CheckAdditionalEmailFormProvider
import pages.additionalEmail.{CheckAdditionalEmailPage, NewAdditionalEmailPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contact.CheckAdditionalEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckAdditionalEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: CheckAdditionalEmailFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: CheckAdditionalEmailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val emailAddress = request.userAnswers.flatMap(_.get(NewAdditionalEmailPage)).getOrElse("")
    val preparedForm = request.userAnswers.flatMap(_.get(CheckAdditionalEmailPage)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    Ok(view(preparedForm, emailAddress))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val emailAddress = request.userAnswers.flatMap(_.get(NewAdditionalEmailPage)).getOrElse("")
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, emailAddress))),
        value =>
          if (value) {
            for {
              emailAdded <- tradeReportingExtractsService.addAdditionalEmail(request.eori, emailAddress)
              _          <- if (emailAdded) {
                              request.userAnswers.fold(Future.unit) { userAnswers =>
                                Future
                                  .fromTry(userAnswers.remove(NewAdditionalEmailPage))
                                  .flatMap(sessionRepository.set)
                              }
                            } else {
                              Future.failed(new RuntimeException("Failed to add additional email"))
                            }
            } yield Redirect(controllers.contact.routes.AdditionalEmailAddedController.onPageLoad(emailAddress))
          } else {
            Future.successful(Redirect(controllers.contact.routes.NewAdditionalEmailController.onPageLoad()))
          }
      )
  }
}
