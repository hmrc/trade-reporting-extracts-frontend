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
import models.{Mode, SelectThirdPartyEori}
import models.report.{Decision, ReportRequestSection, ReportTypeImport}
import navigation.{Navigator, ReportNavigator}
import pages.report.{DecisionPage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.problem.NoThirdPartyAccessView
import views.html.report.SelectThirdPartyEoriView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
  view: SelectThirdPartyEoriView,
  problemView: NoThirdPartyAccessView
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
          val cleanedAnswers = ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers)
          for {
            _ <- sessionRepository.set(cleanedAnswers)
          } yield Ok(problemView())
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
            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers
                                    .get(DecisionPage)
                                    .map {
                                      case Decision.Import =>
                                        request.userAnswers.set(SelectThirdPartyEoriPage, value)
                                      case Decision.Export =>
                                        request.userAnswers
                                          .set(SelectThirdPartyEoriPage, value)
                                          .flatMap(_.set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem)))
                                    }
                                    .getOrElse(request.userAnswers.set(SelectThirdPartyEoriPage, value))
                                )
              redirectUrl     = navigator.nextPage(SelectThirdPartyEoriPage, mode, updatedAnswers).url
              answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
              _              <- sessionRepository.set(answersWithNav)
            } yield Redirect(navigator.nextPage(SelectThirdPartyEoriPage, mode, answersWithNav))
        )
  }
}
