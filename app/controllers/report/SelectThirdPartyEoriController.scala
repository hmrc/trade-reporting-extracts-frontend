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
import forms.report.SelectThirdPartyEoriFormProvider
import models.report.{Decision, ReportRequestSection, ReportTypeImport}
import models.{Mode, UserAnswers}
import navigation.ReportNavigator
import pages.report.{DecisionPage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ErrorHandlers
import views.html.report.SelectThirdPartyEoriView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SelectThirdPartyEoriController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SelectThirdPartyEoriFormProvider,
  reportRequestSection: ReportRequestSection,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: SelectThirdPartyEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val preparedForm = request.userAnswers.get(SelectThirdPartyEoriPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      tradeReportingExtractsService.getSelectThirdPartyEori(request.eori).flatMap { selectThirdPartyEori =>
        if (selectThirdPartyEori.values.isEmpty) {
          for {
            cleanedAnswers <-
              Future.successful(ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers))
            _              <- sessionRepository.set(cleanedAnswers)
          } yield Redirect(controllers.problem.routes.NoThirdPartyAccessController.onPageLoad())
        } else {
          Future.successful(Ok(view(preparedForm, mode, selectThirdPartyEori)))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            tradeReportingExtractsService.getSelectThirdPartyEori(request.eori).map { selectThirdPartyEori =>
              BadRequest(view(formWithErrors, mode, selectThirdPartyEori))
            },
          value =>
            (for {
              thirdPartyDetails <- tradeReportingExtractsService.getAuthorisedBusinessDetails(request.eori, value)
              updatedAnswers    <-
                Future.fromTry(updateAnswersBasedOnDataTypes(request.userAnswers, value, thirdPartyDetails.dataTypes))
              redirectUrl        = navigator.nextPage(SelectThirdPartyEoriPage, mode, updatedAnswers).url
              answersWithNav     = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
              _                 <- sessionRepository.set(answersWithNav)
            } yield Redirect(navigator.nextPage(SelectThirdPartyEoriPage, mode, answersWithNav)))
              .recoverWith(ErrorHandlers.handleNoAuthorisedUserFoundException(request, sessionRepository, value))
        )
  }

  private def updateAnswersBasedOnDataTypes(
    userAnswers: UserAnswers,
    value: String,
    dataTypes: Set[String]
  ): Try[UserAnswers] =
    dataTypes match {
      case s if s == Set("exports") =>
        userAnswers
          .set(SelectThirdPartyEoriPage, value)
          .flatMap(_.set(DecisionPage, Decision.Export))
          .flatMap(_.set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem)))
      case s if s == Set("imports") =>
        userAnswers
          .set(SelectThirdPartyEoriPage, value)
          .flatMap(_.set(DecisionPage, Decision.Import))
      case _                        => userAnswers.remove(DecisionPage).flatMap(_.set(SelectThirdPartyEoriPage, value))
    }
}
