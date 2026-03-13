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
import models.report.{EmailSelection, ReportConfirmation, SubmissionMeta}
import models.requests.DataRequest
import pages.report.{EmailSelectionPage, NewEmailNotificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{DateTimeFormats, ReportHelpers}
import views.html.report.RequestConfirmationView

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.Future

class RequestConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RequestConfirmationView,
  config: FrontendAppConfig,
  clock: Clock
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val surveyUrl = config.exitSurveyUrl

    val submissionMeta = request.userAnswers.submissionMeta
      .map(_.as[SubmissionMeta])
      .getOrElse(SubmissionMeta(Seq.empty, Instant.now(), false))

    val isMoreThanOneReport = submissionMeta.isMoreThanOneReport

    val (submittedDate, submittedTime) =
      DateTimeFormats.instantToDateAndTime(submissionMeta.submittedAt, clock)
    Future.successful(
      Ok(
        view(
          submissionMeta.allEmails,
          isMoreThanOneReport,
          transformReportConfirmations(submissionMeta.reportConfirmations),
          surveyUrl,
          submittedDate,
          submittedTime
        )
      )
    )
  }

  private def transformReportConfirmations(
    reportConfirmations: Seq[ReportConfirmation]
  ): Seq[ReportConfirmation] =
    reportConfirmations.map { rc =>
      val newType = rc.reportType match {
        case "importItem"    => "reportTypeImport.importItem"
        case "importHeader"  => "reportTypeImport.importHeader"
        case "importTaxLine" => "reportTypeImport.importTaxLine"
        case "exportItem"    => "reportTypeImport.exportItem"
        case _               => ""
      }
      rc.copy(reportType = newType)
    }
}
