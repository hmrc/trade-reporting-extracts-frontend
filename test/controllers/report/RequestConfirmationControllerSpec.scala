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
import exceptions.NoAuthorisedUserFoundException
import models.report.{ReportConfirmation, ReportRequestUserAnswersModel, ReportTypeImport}
import models.thirdparty.AuthorisedThirdPartiesViewModel
import models.{AlreadySubmittedFlag, NotificationEmail, SelectThirdPartyEori, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.report.{EmailSelectionPage, NewEmailNotificationPage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import play.api.i18n.Lang
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ReportRequestDataService, TradeReportingExtractsService}
import utils.DateTimeFormats.formattedSystemTime
import views.html.report.RequestConfirmationView

import java.time.{Clock, Instant, LocalDateTime, ZoneId, ZoneOffset}
import scala.concurrent.Future

class RequestConfirmationControllerSpec extends SpecBase with MockitoSugar {

  val mockSessionRepository: SessionRepository                         = mock[SessionRepository]
  val mockReportRequestDataService: ReportRequestDataService           = mock[ReportRequestDataService]
  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]

  val fixedInstant: Instant = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.systemDefault())

  "RequestConfirmationController" - {

    "must redirect to already submitted controller when already submitted flag exists" in {
      val userAnswers = UserAnswers("id").set(AlreadySubmittedFlag(), true).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.AlreadySubmittedController.onPageLoad().url
      }
    }

    "must return OK and the correct view when EmailSelectionPage is defined - single report" in {

      val newEmail          = "new.email@example.com"
      val selectedEmails    = Seq("email1@example.com", "email2@example.com", newEmail)
      val emailString       = selectedEmails.mkString(", ")
      val notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now())

      val userAnswers = UserAnswers("id")
        .set(EmailSelectionPage, selectedEmails.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, newEmail)
        .success
        .value

      when(mockReportRequestDataService.buildReportRequest(any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importTaxLine"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com"))
        )
      )
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq(ReportConfirmation("MyReport", "importTaxLine", "RE00000001"))))
      when(mockTradeReportingExtractsService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(notificationEmail))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          Some(emailString),
          false,
          Seq(ReportConfirmation("MyReport", "reportTypeImport.importTaxLine", "RE00000001")),
          surveyUrl,
          notificationEmail.address,
          "5 May 2025",
          formattedSystemTime(fixedClock)(Lang("en"))
        )(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("MyReport") mustBe true
        contentAsString(result).contains("import tax line") mustBe true
        contentAsString(result).contains("RE00000001") mustBe true
        contentAsString(result).contains(
          "You have requested an import tax line report named MyReport. The reference number is RE00000001"
        ) mustBe true
        contentAsString(result).contains("check the status of your requested report") mustBe true
        contentAsString(result).contains("check the status of your requested reports") mustBe false
        contentAsString(result).contains("We’ll also send the email to") mustBe true
        contentAsString(result).contains("when your report is ready to download") mustBe true
        contentAsString(result).contains("when your reports are ready to download") mustBe false
      }
    }

    "must return OK and the correct view when EmailSelectionPage is defined - multiple reports" in {

      val newEmail          = "new.email@example.com"
      val selectedEmails    = Seq("email1@example.com", "email2@example.com", newEmail)
      val emailString       = selectedEmails.mkString(", ")
      val notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now())

      val userAnswers = UserAnswers("id")
        .set(EmailSelectionPage, selectedEmails.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, newEmail)
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem, ReportTypeImport.ImportHeader))
        .success
        .value

      when(mockReportRequestDataService.buildReportRequest(any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader, importItem"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com"))
        )
      )
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(
          Future.successful(
            Seq(
              ReportConfirmation("MyReport", "importHeader", "RE00000001"),
              ReportConfirmation("MyReport", "importItem", "RE00000002")
            )
          )
        )
      when(mockTradeReportingExtractsService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(notificationEmail))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          Some(emailString),
          true,
          Seq(
            ReportConfirmation("MyReport", "reportTypeImport.importHeader", "RE00000001"),
            ReportConfirmation("MyReport", "reportTypeImport.importItem", "RE00000002")
          ),
          surveyUrl,
          notificationEmail.address,
          "5 May 2025",
          formattedSystemTime(fixedClock)(Lang("en"))
        )(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("MyReport") mustBe true
        contentAsString(result).contains("Import header") mustBe true
        contentAsString(result).contains("RE00000001") mustBe true
        contentAsString(result).contains("Import item") mustBe true
        contentAsString(result).contains("RE00000002") mustBe true
        contentAsString(result).contains(
          "You have requested an import header named MyReport. The reference number is RE00000001"
        ) mustBe false
        contentAsString(result).contains("check the status of your requested report") mustBe true
        contentAsString(result).contains("check the status of your requested reports") mustBe true
        contentAsString(result).contains("We’ll also send the email to") mustBe true
        contentAsString(result).contains("when your report is ready to download") mustBe false
        contentAsString(result).contains("when your reports are ready to download") mustBe true
      }
    }

    "must return OK and correct view when EmailSelectionPage is not defined" in {

      when(mockReportRequestDataService.buildReportRequest(any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "export",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("exportItem"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com"))
        )
      )
      val notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now())
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq(ReportConfirmation("MyReport", "exportItem", "RE00000001"))))
      when(mockTradeReportingExtractsService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(notificationEmail))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = UserAnswers("id")

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl

        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          None,
          false,
          Seq(ReportConfirmation("MyReport", "reportTypeImport.exportItem", "RE00000001")),
          surveyUrl,
          notificationEmail.address,
          "5 May 2025",
          formattedSystemTime(fixedClock)(Lang("en"))
        )(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("We’ll also send the email to") mustBe false
      }
    }

    "must redirect to RequestNotCompletedPage when SelectThirdPartyEoriPage is defined but access revoked before submission" in {

      val userAnswers = UserAnswers("id")
        .set(SelectThirdPartyEoriPage, "GB123456789000")
        .success
        .value

      when(mockReportRequestDataService.buildReportRequest(any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "export",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("exportItem"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com"))
        )
      )
      val notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now())
      when(mockTradeReportingExtractsService.createReportRequest(any())(any()))
        .thenReturn(Future.successful(Seq(ReportConfirmation("MyReport", "exportItem", "RE00000001"))))
      when(mockTradeReportingExtractsService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(notificationEmail))
      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.failed(new NoAuthorisedUserFoundException("No access")))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReportRequestDataService].toInstance(mockReportRequestDataService),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.report.routes.RequestNotCompletedController
          .onPageLoad("GB123456789000")
          .url
      }
    }
  }
}
