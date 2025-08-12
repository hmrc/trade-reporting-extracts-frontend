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
import models.availableReports.{AvailableReportAction, AvailableReportsViewModel, AvailableThirdPartyReportsViewModel, AvailableUserReportsViewModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.should
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HttpEntity
import play.api.inject.bind
import play.api.mvc.{ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.AvailableReportsView

import java.time.Instant
import scala.concurrent.Future

class AvailableReportsControllerSpec extends SpecBase with MockitoSugar {

  "AvailableReports Controller" - {

    "must return OK and the correct view for a GET when no reports available" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      when(mockTradeReportingExtractsService.getAvailableReports(any())(any()))
        .thenReturn(Future.successful(AvailableReportsViewModel(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.AvailableReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AvailableReportsView]

        val reports = AvailableReportsViewModel(None, None)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(reports, false, false)(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("There are no reports available to download yet") mustBe true
      }
    }

    "must return OK and the correct view for a GET when both types of reports reports available" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val userReport = AvailableUserReportsViewModel(
        reportName = "reportName",
        referenceNumber = "referenceNumber",
        reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
        expiryDate = Instant.parse("2024-01-01T00:00:00Z"),
        action = Seq.empty[AvailableReportAction]
      )

      val thirdPartyReport = AvailableThirdPartyReportsViewModel(
        reportName = "reportName",
        referenceNumber = "referenceNumber",
        companyName = "businessName",
        reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
        expiryDate = Instant.parse("2024-01-01T00:00:00Z"),
        action = Seq.empty[AvailableReportAction]
      )

      when(mockTradeReportingExtractsService.getAvailableReports(any())(any())).thenReturn(
        Future.successful(
          AvailableReportsViewModel(
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
        val request = FakeRequest(GET, controllers.routes.AvailableReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AvailableReportsView]

        val reports =
          AvailableReportsViewModel(
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
      when(mockTradeReportingExtractsService.getAvailableReports(any())(any())).thenReturn(
        Future.successful(
          AvailableReportsViewModel(
            Some(
              Seq(
                AvailableUserReportsViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  expiryDate = Instant.parse("2024-01-01T00:00:00Z"),
                  action = Seq.empty[AvailableReportAction]
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
        val request = FakeRequest(GET, controllers.routes.AvailableReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AvailableReportsView]

        val reports =
          AvailableReportsViewModel(
            Some(
              Seq(
                AvailableUserReportsViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  expiryDate = Instant.parse("2024-01-01T00:00:00Z"),
                  action = Seq.empty[AvailableReportAction]
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
      when(mockTradeReportingExtractsService.getAvailableReports(any())(any())).thenReturn(
        Future.successful(
          AvailableReportsViewModel(
            None,
            Some(
              Seq(
                AvailableThirdPartyReportsViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  companyName = "businessName",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  expiryDate = Instant.parse("2024-01-01T00:00:00Z"),
                  action = Seq.empty[AvailableReportAction]
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
        val request = FakeRequest(GET, controllers.routes.AvailableReportsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AvailableReportsView]

        val reports =
          AvailableReportsViewModel(
            None,
            Some(
              Seq(
                AvailableThirdPartyReportsViewModel(
                  reportName = "reportName",
                  referenceNumber = "referenceNumber",
                  companyName = "businessName",
                  reportType = ReportTypeName.IMPORTS_ITEM_REPORT,
                  expiryDate = Instant.parse("2024-01-01T00:00:00Z"),
                  action = Seq.empty[AvailableReportAction]
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

    "must return download response when audit download file is called" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      val downloadResponse                  = Result(
        ResponseHeader(200),
        HttpEntity.NoEntity
      )

      when(mockTradeReportingExtractsService.downloadFile(any(), any(), any())(any()))
        .thenReturn(Future.successful(downloadResponse))

      when(mockTradeReportingExtractsService.auditReportDownload(any(), any(), any())(any()))
        .thenReturn(Future.successful(NO_CONTENT))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.routes.AvailableReportsController.auditDownloadFile("file", "fileName", "reportReference").url
        )
        val result  = route(application, request).value

        status(result) mustEqual OK
        verify(mockTradeReportingExtractsService, times(1))
          .downloadFile(eqTo("file"), eqTo("fileName"), eqTo("reportReference"))(any[HeaderCarrier])

        verify(mockTradeReportingExtractsService, times(1)).auditReportDownload(
          eqTo("reportReference"),
          eqTo("fileName"),
          eqTo("file")
        )(any[HeaderCarrier])
      }
    }
  }
}
