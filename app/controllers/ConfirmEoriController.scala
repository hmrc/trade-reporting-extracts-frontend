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

package controllers

import controllers.actions.*
import forms.ConfirmEoriFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.ConfirmEoriPage
import pages.thirdparty.EoriNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConfirmEoriView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmEoriController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConfirmEoriFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(ConfirmEoriPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    val eoriNumber   = "GB0000001" // Replace with actual call
    val businessInfo = "" // Replace with actual cal

    Ok(view(preparedForm, mode, eoriNumber, businessInfo))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val eoriNumber = request.userAnswers.get(EoriNumberPage).getOrElse("N/A")

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            tradeReportingExtractsService.getCompanyInformation(eoriNumber).map { businessInfo =>
              BadRequest(view(formWithErrors, mode, eoriNumber, businessInfo.name))
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmEoriPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ConfirmEoriPage, mode, updatedAnswers))
        )
  }

}
