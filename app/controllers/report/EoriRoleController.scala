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
import forms.report.EoriRoleFormProvider
import models.report.{Decision, ReportTypeImport}
import models.{EoriRole, Mode}
import models.report.Decision.Import
import navigation.ReportNavigator
import pages.report.{DecisionPage, EoriRolePage, ReportTypeImportPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.report.EoriRoleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EoriRoleController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EoriRoleFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EoriRoleView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Set[EoriRole]] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm        = request.userAnswers.get(EoriRolePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    val isImporter: Boolean = request.userAnswers.get(DecisionPage).contains(Import)
    Ok(view(preparedForm, mode, isImporter))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val isImporter: Boolean = request.userAnswers.get(DecisionPage).contains(Import)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isImporter))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                                  if (isImporter) {
                                    request.userAnswers.set(EoriRolePage, value)
                                  } else {
                                    request.userAnswers
                                      .set(EoriRolePage, value)
                                      .flatMap(_.set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem)))
                                  }
                                )
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(EoriRolePage, mode, updatedAnswers))
        )
  }
}
