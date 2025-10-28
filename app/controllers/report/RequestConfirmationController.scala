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
import models.AlreadySubmittedFlag
import models.report.{EmailSelection, ReportConfirmation, ReportRequestSection}
import models.requests.DataRequest
import pages.report.{EmailSelectionPage, NewEmailNotificationPage, SelectThirdPartyEoriPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import services.{ReportRequestDataService, TradeReportingExtractsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.DateTimeFormats.{dateTimeFormat, formattedSystemTime}
import utils.ReportHelpers
import views.html.report.RequestConfirmationView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  tradeReportingExtractsService: TradeReportingExtractsService,
  reportRequestDataService: ReportRequestDataService,
  config: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  preventBackNavigationAfterSubmissionAction: PreventBackNavigationAfterSubmissionAction,
  view: RequestConfirmationView,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify
    andThen getData
    andThen requireData
    andThen preventBackNavigationAfterSubmissionAction).async { implicit request =>
    implicit val hc: HeaderCarrier          = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val additionalEmailList: Option[String] = fetchUpdatedData(request)
    val surveyUrl: String                   = config.exitSurveyUrl
    val isMoreThanOneReport                 = ReportHelpers.isMoreThanOneReport(request.userAnswers)
    val (date, time)                        = getDateAndTime

    val maybeThirdPartyEori = request.userAnswers.get(SelectThirdPartyEoriPage)

    val thirdPartyCheck: Future[Boolean] = maybeThirdPartyEori match {
      case Some(thirdPartyEori) =>
        tradeReportingExtractsService
          .getAuthorisedBusinessDetails(request.eori, thirdPartyEori)
          .map(_ => true)
          .recover { case _ => false }
      case None                 => Future.successful(true)
    }

    thirdPartyCheck.flatMap {
      case false =>
        Future.successful(Redirect(controllers.report.routes.RequestNotCompletedController.onPageLoad()))
      case true  =>
        for {
          notificationEmail                <- tradeReportingExtractsService.getNotificationEmail(request.eori)
          reportConfirmations              <- tradeReportingExtractsService.createReportRequest(
                                                reportRequestDataService.buildReportRequest(request.userAnswers, request.eori)
                                              )
          transformedConfirmations          = trasnformReportConfirmations(reportConfirmations)
          updatedAnswers                    = ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers)
          updatedAnswersWithSubmissionFlag <- Future.fromTry(updatedAnswers.set(AlreadySubmittedFlag(), true))
          _                                <- sessionRepository.set(updatedAnswersWithSubmissionFlag)
        } yield Ok(
          view(
            additionalEmailList,
            isMoreThanOneReport,
            transformedConfirmations,
            surveyUrl,
            notificationEmail.address,
            date,
            time
          )
        )
    }
  }

  private def getDateAndTime(implicit messages: Messages): (String, String) =
    (
      LocalDate.now(clock).format(dateTimeFormat()(messages.lang)),
      formattedSystemTime(clock)(messages.lang)
    )

  private def trasnformReportConfirmations(
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

  private def fetchUpdatedData(request: DataRequest[AnyContent]): Option[String] =
    request.userAnswers.get(EmailSelectionPage).toSeq.flatMap { selected =>
      selected.map {
        case EmailSelection.AddNewEmailValue =>
          request.userAnswers
            .get(NewEmailNotificationPage)
            .map(HtmlFormat.escape(_).toString)
            .getOrElse("")
        case email                           =>
          HtmlFormat.escape(email).toString
      }
    } match {
      case Nil    => None
      case emails => Some(emails.mkString(", "))
    }
}
