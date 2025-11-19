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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import models.{CompanyInformation, ConsentStatus, ThirdPartyDetails}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.inject.bind
import org.mockito.Mockito.when
import play.api.mvc.RequestHeader
import repositories.SessionRepository
import services.TradeReportingExtractsService
import viewmodels.checkAnswers.thirdparty.{BusinessInfoSummary, DataTheyCanViewSummary, DataTypesSummary, EoriNumberSummary, ThirdPartyAccessPeriodSummary, ThirdPartyReferenceSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.thirdparty.ThirdPartyDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class ThirdPartyDetailsControllerSpec extends SpecBase with MockitoSugar {

  "ThirdPartyDetails Controller" - {

    val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
    val mockFrontendAppConfig: FrontendAppConfig                         = mock[FrontendAppConfig]
    val mockSessionRepository                                            = mock[SessionRepository]

    "must return OK and the correct view for a GET when consent given, no reference" in {

      val thirdPartyDetails = ThirdPartyDetails(
        referenceName = None,
        accessStartDate = LocalDate.of(2025, 1, 1),
        accessEndDate = None,
        dataTypes = Set("import"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("foo", ConsentStatus.Granted)))

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
          )
        )

      when(mockFrontendAppConfig.editThirdPartyEnabled).thenReturn(false)
      when(mockFrontendAppConfig.feedbackUrl(any(classOf[RequestHeader]))).thenReturn("http://localhost/feedback")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad("eori").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyDetailsView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.detailsRow("eori")(messages(application)).get,
            BusinessInfoSummary.row("foo")(messages(application)).get,
            ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when consent given with reference" in {

      val thirdPartyDetails = ThirdPartyDetails(
        referenceName = Some("bar"),
        accessStartDate = LocalDate.of(2025, 1, 1),
        accessEndDate = None,
        dataTypes = Set("import"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("foo", ConsentStatus.Granted)))

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
          )
        )

      when(mockFrontendAppConfig.editThirdPartyEnabled).thenReturn(false)
      when(mockFrontendAppConfig.feedbackUrl(any(classOf[RequestHeader]))).thenReturn("http://localhost/feedback")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad("eori").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyDetailsView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.detailsRow("eori")(messages(application)).get,
            BusinessInfoSummary.row("foo")(messages(application)).get,
            ThirdPartyReferenceSummary.detailsRow(Some("bar"), false)(messages(application)).get,
            ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when editThirdPartyEnabled is true" in {

      val thirdPartyDetails = ThirdPartyDetails(
        referenceName = Some("bar"),
        accessStartDate = LocalDate.of(2025, 1, 1),
        accessEndDate = None,
        dataTypes = Set("import"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("foo", ConsentStatus.Granted)))

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
          )
        )

      when(mockFrontendAppConfig.editThirdPartyEnabled).thenReturn(true)
      when(mockFrontendAppConfig.feedbackUrl(any(classOf[RequestHeader]))).thenReturn("http://localhost/feedback")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad("eori").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyDetailsView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.detailsRow("eori")(messages(application)).get,
            BusinessInfoSummary.row("foo")(messages(application)).get,
            ThirdPartyReferenceSummary.detailsRow(Some("bar"), true)(messages(application)).get,
            ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails, true)(messages(application)).get,
            DataTypesSummary.detailsRow(Set("import"), true, "eori")(messages(application)).get,
            DataTheyCanViewSummary.detailsRow(thirdPartyDetails, true)(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when consent not given with reference" in {

      val thirdPartyDetails = ThirdPartyDetails(
        referenceName = Some("bar"),
        accessStartDate = LocalDate.of(2025, 1, 1),
        accessEndDate = None,
        dataTypes = Set("import"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("foo", ConsentStatus.Denied)))

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
          )
        )

      when(mockFrontendAppConfig.editThirdPartyEnabled).thenReturn(false)
      when(mockFrontendAppConfig.feedbackUrl(any(classOf[RequestHeader]))).thenReturn("http://localhost/feedback")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad("eori").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyDetailsView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.detailsRow("eori")(messages(application)).get,
            ThirdPartyReferenceSummary.detailsRow(Some("bar"), false)(messages(application)).get,
            ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when consent not given with no reference" in {

      val thirdPartyDetails = ThirdPartyDetails(
        referenceName = None,
        accessStartDate = LocalDate.of(2025, 1, 1),
        accessEndDate = None,
        dataTypes = Set("import"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("foo", ConsentStatus.Denied)))

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
          )
        )

      when(mockFrontendAppConfig.editThirdPartyEnabled).thenReturn(false)
      when(mockFrontendAppConfig.feedbackUrl(any(classOf[RequestHeader]))).thenReturn("http://localhost/feedback")

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad("eori").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyDetailsView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.detailsRow("eori")(messages(application)).get,
            ThirdPartyReferenceSummary.detailsRow(None, false)(messages(application)).get,
            ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary.detailsRow(thirdPartyDetails, false)(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true)(request, messages(application)).toString
      }
    }
  }
}
