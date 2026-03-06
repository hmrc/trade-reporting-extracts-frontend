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
import models.{AlreadySubmittedFlag, SectionNavigation}
import models.report.{EmailSelection, ReportRequestSection, SubmissionMeta}
import pages.report.{EmailSelectionPage, NewEmailNotificationPage}
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ReportRequestDataService, TradeReportingExtractsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ReportHelpers

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmitReportController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  tradeReportingExtractsService: TradeReportingExtractsService,
  reportRequestDataService: ReportRequestDataService,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    reportRequestDataService.buildReportRequest(request.userAnswers, request.eori) match {
      case Some(reportRequest) =>
        for {
          notificationEmail     <- tradeReportingExtractsService.getNotificationEmail(request.eori)
          reportConfirmations   <- tradeReportingExtractsService.createReportRequest(reportRequest)
          updatedAnswers         = ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers)
          additionalEmails       = collectAdditionalEmails(request.userAnswers)
          allEmails              = notificationEmail.address +: additionalEmails
          submissionMetaModel    = SubmissionMeta(
                                     reportConfirmations = reportConfirmations,
                                     submittedAt = Instant.now(clock),
                                     isMoreThanOneReport = ReportHelpers.isMoreThanOneReport(request.userAnswers),
                                     allEmails = allEmails
                                   )
          updatedAnswersWithFlag = updatedAnswers.set(AlreadySubmittedFlag(), true).get
          userAnswersWithMeta    = updatedAnswersWithFlag.copy(
                                     submissionMeta = Some(Json.toJson(submissionMetaModel).as[JsObject])
                                   )
          _                     <- sessionRepository.set(userAnswersWithMeta)
        } yield Redirect(controllers.report.routes.RequestConfirmationController.onPageLoad())

      case None =>
        val cleanedAnswers = ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers)
        sessionRepository.set(cleanedAnswers) map { _ =>
          Redirect(controllers.problem.routes.ReportRequestIssueController.onPageLoad())
        }
    }
  }

  private def collectAdditionalEmails(userAnswers: models.UserAnswers): Seq[String] = {
    val emails = userAnswers.get(EmailSelectionPage).toSeq.flatMap { selectedStrings =>
      selectedStrings.flatMap {
        case EmailSelection.AddNewEmailValue =>
          // This is the "AddNewEmail" option - get the new email from NewEmailNotificationPage
          userAnswers
            .get(NewEmailNotificationPage)
            .map(_.trim)
            .filter(_.nonEmpty)
        case emailString                     =>
          // This is an existing email address
          val trimmedEmail = emailString.trim
          if (trimmedEmail.nonEmpty && trimmedEmail != EmailSelection.AddNewEmailValue) {
            Some(trimmedEmail)
          } else {
            None
          }
      }
    }
    emails
  }

}
