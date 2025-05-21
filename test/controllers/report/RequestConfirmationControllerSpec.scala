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
import utils.ReportHelpers
import views.html.report.RequestConfirmationView

import scala.concurrent.Future

class RequestConfirmationControllerSpec extends SpecBase with MockitoSugar {

  val mockSessionRepository             = mock[SessionRepository]
  val mockReportRequestDataService      = mock[ReportRequestDataService]
  val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  val mockReportHelpers                 = mock[ReportHelpers]

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

      when(mockReportRequestDataService.buildReportRequest(any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com"))
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
        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          Seq("email1@example.com", "email2@example.com", newEmail),
          false,
          "reference"
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and an empty list when EmailSelectionPage is not defined" in {

      when(mockReportRequestDataService.buildReportRequest(any(), any())).thenReturn(
        ReportRequestUserAnswersModel(
          eori = "eori",
          dataType = "import",
          whichEori = Some("eori"),
          eoriRole = Set("declarant"),
          reportType = Set("importHeader"),
          reportStartDate = "2025-04-16",
          reportEndDate = "2025-05-16",
          reportName = "MyReport",
          additionalEmail = Some(Set("email@email.com"))
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
        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty, false, "reference")(request, messages(application)).toString
      }
    }
  }
}
