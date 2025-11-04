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

import config.FrontendAppConfig
import controllers.actions.*
import models.NormalMode
import models.report.ChooseEori.Myauthority
import models.report.{ChooseEori, ReportRequestSection}
import pages.report.{ChooseEoriPage, CustomRequestStartDatePage, ReportDateRangePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.report.ExportItemReportView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ExportItemReportController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  config: FrontendAppConfig,
  view: ExportItemReportView,
  reportRequestSection: ReportRequestSection,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    val continueUrl = request.userAnswers.get(ReportDateRangePage) match {
      case Some(_) => request.userAnswers.get(ChooseEoriPage) match {
        case Some(Myauthority) if request.userAnswers.get(CustomRequestStartDatePage).isEmpty => controllers.report.routes.CustomRequestStartDateController.onPageLoad(NormalMode).url
        case _ => controllers.report.routes.CheckYourAnswersController.onPageLoad().url
      }
      case None => controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode).url
    }

    val answersWithNav = reportRequestSection.saveNavigation(request.userAnswers, continueUrl)
    sessionRepository.set(answersWithNav) map { _ =>
      Ok(view(config.guidanceWhatsInTheReportUrl, continueUrl))
    }
  }
}
