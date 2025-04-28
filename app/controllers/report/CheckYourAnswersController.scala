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

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Mode
import navigation.ReportNavigator
import pages.report.CheckYourAnswersPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.report.*
import viewmodels.govuk.summarylist.*
import views.html.report.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  navigator: ReportNavigator,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends BaseController {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val rows: Seq[Option[SummaryListRow]] = Seq(
      DecisionSummary.row(request.userAnswers),
      ChooseEoriSummary.row(request.userAnswers),
      EoriRoleSummary.row(request.userAnswers),
      ReportTypeImportSummary.row(request.userAnswers),
      ReportDateRangeSummary.row(request.userAnswers),
      ReportNameSummary.row(request.userAnswers),
      MaybeAdditionalEmailSummary.row(request.userAnswers)
    )
    val list                              = SummaryListViewModel(rows = rows.flatten)
    Ok(view(list))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      Future.successful {
        Redirect(navigator.nextPage(CheckYourAnswersPage, mode, userAnswers = request.userAnswers))
      }
  }
}
