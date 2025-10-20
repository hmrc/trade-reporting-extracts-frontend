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
import forms.report.MaybeAdditionalEmailFormProvider
import models.Mode
import models.report.ReportRequestSection
import navigation.ReportNavigator
import pages.report.MaybeAdditionalEmailPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportHelpers
import views.html.report.MaybeAdditionalEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MaybeAdditionalEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: MaybeAdditionalEmailFormProvider,
  reportRequestSection: ReportRequestSection,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: MaybeAdditionalEmailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val isMoreThanOneReport = ReportHelpers.isMoreThanOneReport(request.userAnswers)

      val preparedForm = request.userAnswers.get(MaybeAdditionalEmailPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      tradeReportingExtractsService.getNotificationEmail(request.eori).flatMap { response =>
        Future.successful(Ok(view(preparedForm, mode, isMoreThanOneReport, response.address)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val isMoreThanOneReport = ReportHelpers.isMoreThanOneReport(request.userAnswers)
      tradeReportingExtractsService.getNotificationEmail(request.eori).flatMap { response =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, isMoreThanOneReport, response.address))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MaybeAdditionalEmailPage, value))
                redirectUrl     = navigator.nextPage(MaybeAdditionalEmailPage, mode, updatedAnswers).url
                answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                _              <- sessionRepository.set(answersWithNav)
              } yield Redirect(navigator.nextPage(MaybeAdditionalEmailPage, mode, answersWithNav))
          )
      }
  }
}
