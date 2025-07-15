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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.report.DecisionFormProvider
import models.{Mode, UserAnswers}
import models.report.{ChooseEori, Decision, ReportRequestSection}
import navigation.ReportNavigator
import pages.report.{ChooseEoriPage, DecisionPage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.report.DecisionView
import config.FrontendAppConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class DecisionController @Inject() (
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  view: DecisionView,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  belowReportRequestLimitAction: BelowReportRequestLimitAction,
  formProvider: DecisionFormProvider,
  navigator: ReportNavigator,
  reportRequestSection: ReportRequestSection,
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends BaseController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen belowReportRequestLimitAction) { implicit request =>
      val preparedForm = request.userAnswers.get(DecisionPage).fold(form)(form.fill)
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen belowReportRequestLimitAction).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val updatedAnswersTry: Try[UserAnswers] = for {
              withDecision  <- request.userAnswers.set(DecisionPage, value)
              enriched      <- if (!appConfig.thirdPartyEnabled) {
                                 withDecision.set(ChooseEoriPage, ChooseEori.Myeori)
                               } else {
                                 Success(withDecision)
                               }
              redirectUrl    = navigator.nextPage(DecisionPage, mode, enriched).url
              answersWithNav = reportRequestSection.saveNavigation(enriched, redirectUrl)
            } yield answersWithNav

            sessionRepository.set(updatedAnswersTry.get).map { _ =>
              Redirect(navigator.nextPage(DecisionPage, mode, updatedAnswersTry.get))
            }
          }
        )
    }
}
