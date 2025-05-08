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
import forms.report.AccountsYouHaveAuthorityOverImportFormProvider
import models.report.Decision
import models.{Mode, ReportTypeImport}
import navigation.ReportNavigator
import pages.report.{AccountsYouHaveAuthorityOverImportPage, DecisionPage, ReportTypeImportPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInput
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.report.AccountsYouHaveAuthorityOverImportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccountsYouHaveAuthorityOverImportController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AccountsYouHaveAuthorityOverImportFormProvider,
  val controllerComponents: MessagesControllerComponents,
  govukInput: GovukInput,
  view: AccountsYouHaveAuthorityOverImportView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = formProvider()

      val preparedForm = request.userAnswers.get(AccountsYouHaveAuthorityOverImportPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      tradeReportingExtractsService.getEoriList().map { eoriList =>
        Ok(view(preparedForm, mode, eoriList))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val form = formProvider()

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            tradeReportingExtractsService.getEoriList().map { eoriList =>
              BadRequest(view(formWithErrors, mode, eoriList))
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers
                                    .get(DecisionPage)
                                    .map {
                                      case Decision.Import =>
                                        request.userAnswers.set(AccountsYouHaveAuthorityOverImportPage, value)
                                      case Decision.Export =>
                                        request.userAnswers
                                          .set(AccountsYouHaveAuthorityOverImportPage, value)
                                          .flatMap(_.set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem)))
                                    }
                                    .getOrElse(request.userAnswers.set(AccountsYouHaveAuthorityOverImportPage, value))
                                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AccountsYouHaveAuthorityOverImportPage, mode, updatedAnswers))
        )
  }
}
