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

import controllers.BaseController
import controllers.actions.*
import forms.report.EmailSelectionFormProvider
import models.Mode
import models.report.{EmailSelection, ReportRequestSection}
import navigation.ReportNavigator
import pages.report.EmailSelectionPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.report.EmailSelectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailSelectionController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EmailSelectionFormProvider,
  reportRequestSection: ReportRequestSection,
  val controllerComponents: MessagesControllerComponents,
  view: EmailSelectionView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends BaseController {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      tradeReportingExtractsService.setupUser(request.eori).flatMap { userDetails =>
        val dynamicEmails = userDetails.additionalEmails
        if dynamicEmails.nonEmpty then
          val form         = formProvider(dynamicEmails)
          val preparedForm = request.userAnswers.get(EmailSelectionPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Future.successful(Ok(view(preparedForm, mode, dynamicEmails)))
        else
          for {
            updatedAnswers <-
              Future.fromTry(request.userAnswers.set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue)))
            redirectUrl     = navigator.nextPage(EmailSelectionPage, mode, updatedAnswers).url
            answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
            _              <- sessionRepository.set(answersWithNav)
          } yield Redirect(navigator.nextPage(EmailSelectionPage, mode, answersWithNav))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      tradeReportingExtractsService.setupUser(request.eori).flatMap { userDetails =>
        val dynamicEmails = userDetails.additionalEmails
        val form          = formProvider(dynamicEmails)

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, dynamicEmails))),
            selectedValues =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(EmailSelectionPage, selectedValues))
                redirectUrl     = navigator.nextPage(EmailSelectionPage, mode, updatedAnswers).url
                answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                _              <- sessionRepository.set(answersWithNav)
              } yield Redirect(navigator.nextPage(EmailSelectionPage, mode, answersWithNav))
          )
      }
    }
}
