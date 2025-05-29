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
import models.ReportTypeName
import models.report.*
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import views.html.report.RequestedReportsView

import java.time.Instant
import scala.concurrent.Future

class RequestedReportsControllerSpec extends SpecBase with MockitoSugar {

  "RequestedReports Controller" - {

    "must return OK and the correct view for a GET when no reports available" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      when(mockTradeReportingExtractsService.getRequestedReports(any())(any()))
        .thenReturn(Future.successful(RequestedReportsViewModel(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestedReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequestedReportsView]

        val reports = RequestedReportsViewModel(None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reports, false, false)(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("No reports have been requested yet") mustBe true
      }
    }

    "must return OK and the correct view for a GET when both types of reports reports available" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val userReport = RequestedUserReportViewModel(
        reportName = "reportName",
        referenceNumber = "referenceNumber",
        reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
        requestedDate = Instant.parse("2024-01-01T00:00:00Z")
      )

      val thirdPartyReport = RequestedThirdPartyReportViewModel(
        reportName = "reportName",
        referenceNumber = "referenceNumber",
        companyName = "businessName",
        reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
        requestedDate = Instant.parse("2024-01-01T00:00:00Z")
      )

      when(mockTradeReportingExtractsService.getRequestedReports(any())(any())).thenReturn(
        Future.successful(
          RequestedReportsViewModel(
            Some(Seq(userReport)),
            Some(Seq(thirdPartyReport))
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestedReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequestedReportsView]

        val reports =
          RequestedReportsViewModel(
            Some(Seq(userReport)),
            Some(Seq(thirdPartyReport))
          )

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-tabs__list-item").size() mustBe 2

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reports, true, true)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return correct view for a GET when only user reports available" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getRequestedReports(any())(any())).thenReturn(
        Future.successful(
          RequestedReportsViewModel(
            Some(
              Seq(
                RequestedUserReportViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  requestedDate = Instant.parse("2024-01-01T00:00:00Z")
                )
              )
            ),
            None
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestedReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequestedReportsView]

        val reports =
          RequestedReportsViewModel(
            Some(
              Seq(
                RequestedUserReportViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  requestedDate = Instant.parse("2024-01-01T00:00:00Z")
                )
              )
            ),
            None
          )

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-tabs__list-item").size() mustBe 0
        contentAsString(result).contains("My reports") mustBe true
        contentAsString(result).contains("Third party reports") mustBe false

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reports, true, false)(
          request,
          messages(application)
        ).toString

      }
    }

    "must return OK and the correct view for a GET when only user third party reports available" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getRequestedReports(any())(any())).thenReturn(
        Future.successful(
          RequestedReportsViewModel(
            None,
            Some(
              Seq(
                RequestedThirdPartyReportViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  companyName = "businessName",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  requestedDate = Instant.parse("2024-01-01T00:00:00Z")
                )
              )
            )
          )
        )
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestedReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequestedReportsView]

        val reports =
          RequestedReportsViewModel(
            None,
            Some(
              Seq(
                RequestedThirdPartyReportViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  companyName = "businessName",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  requestedDate = Instant.parse("2024-01-01T00:00:00Z")
                )
              )
            )
          )

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-tabs__list-item").size() mustBe 0
        contentAsString(result).contains("My reports") mustBe false
        contentAsString(result).contains("Third party reports") mustBe true

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reports, false, true)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
