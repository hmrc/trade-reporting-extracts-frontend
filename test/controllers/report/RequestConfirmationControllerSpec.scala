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

import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import models.report.{EmailSelection, ReportRequestUserAnswersModel}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.report.{EmailSelectionPage, NewEmailNotificationPage}
import play.api.test.FakeRequest
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ReportRequestDataService, TradeReportingExtractsService}
import uk.gov.hmrc.auth.core.retrieve.ItmpName
import utils.ReportHelpers
import views.html.report.RequestConfirmationView

import java.net.URLEncoder
import scala.concurrent.Future

class RequestConfirmationControllerSpec extends SpecBase with MockitoSugar {

  val mockSessionRepository: SessionRepository                         = mock[SessionRepository]
  val mockReportRequestDataService: ReportRequestDataService           = mock[ReportRequestDataService]
  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]

  "RequestConfirmationController" - {

    "must return OK and the correct view when EmailSelectionPage is defined" in {

      val emailSelection = Seq(EmailSelection.Email1, EmailSelection.Email3)
      val newEmail       = "new.email@example.com"

      val userAnswers = UserAnswers("id")
        .set(EmailSelectionPage, EmailSelection.values.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, newEmail)
        .success
        .value

      when(mockReportRequestDataService.buildReportRequest(any(), any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com")),
          itmpName = Some("Test User")
        )
      )
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq("reference")))

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustBe view(
          Seq("email1@example.com", "email2@example.com", newEmail),
          false,
          "reference",
          surveyUrl
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and an empty list when EmailSelectionPage is not defined" in {

      when(mockReportRequestDataService.buildReportRequest(any(), any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com")),
          itmpName = Some("Test User")
        )
      )
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq("reference")))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = UserAnswers("id")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty, false, "reference", surveyUrl)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return and display a singular reference when only one report type " in {

      when(mockReportRequestDataService.buildReportRequest(any(), any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com")),
          itmpName = Some("Test User")
        )
      )
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq("RE00000001")))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = UserAnswers("id")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty, false, "RE00000001", surveyUrl)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return and display a multiple references when more than one report type " in {

      when(mockReportRequestDataService.buildReportRequest(any(), any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader", "importItem"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com")),
          itmpName = Some("Test User")
        )
      )
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq("RE00000001", "RE00000002")))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = UserAnswers("id")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty, false, "RE00000001, RE00000002", surveyUrl)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
