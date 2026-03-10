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
import models.report.ChooseEori.{Myauthority, Myeori}
import models.{AlreadySubmittedFlag, SectionNavigation}
import models.report.{ReportRequestSection, SubmissionMeta}
import models.requests.DataRequest
import pages.report.{ChooseEoriPage, SelectThirdPartyEoriPage}
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ReportRequestDataService, TradeReportingExtractsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ErrorHandlers, ReportHelpers}

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
    (request.userAnswers.get(ChooseEoriPage), request.userAnswers.get(SelectThirdPartyEoriPage)) match {
      case (Some(Myeori), _)                     => SubmitReportRequest(request)
      case (Some(Myauthority), Some(traderEori)) =>
        tradeReportingExtractsService
          .getAuthorisedBusinessDetails(request.eori, traderEori)
          .flatMap(_ => SubmitReportRequest(request))
          .recoverWith(ErrorHandlers.handleNoAuthorisedUserFoundException(request, sessionRepository, traderEori))
      case (_, _)                                =>
        sessionRepository.set(
          ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers)
        ) map { _ =>
          Redirect(controllers.problem.routes.ReportRequestIssueController.onPageLoad())
        }
    }
  }

  private def SubmitReportRequest(request: DataRequest[AnyContent])(implicit hc: HeaderCarrier) =
    reportRequestDataService.buildReportRequest(request.userAnswers, request.eori) match {
      case Some(reportRequest) =>
        for {
          notificationEmail     <- tradeReportingExtractsService.getNotificationEmail(request.eori)
          reportConfirmations   <- tradeReportingExtractsService.createReportRequest(reportRequest)
          updatedAnswers         = ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers)
          submissionMetaModel    = SubmissionMeta(
                                     reportConfirmations = reportConfirmations,
                                     notificationEmail = notificationEmail.address,
                                     submittedAt = Instant.now(clock),
                                     isMoreThanOneReport = ReportHelpers.isMoreThanOneReport(request.userAnswers)
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
