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
import controllers.actions.{CustomFakeDataRetrievalOrCreateAction, DataRetrievalOrCreateAction, FakeDataRetrievalOrCreateAction, FakeIdentifierAction, IdentifierAction}
import models.{CompanyInformation, ConsentStatus, ThirdPartyDetails, UserAnswers}
import models.{CompanyInformation, ConsentStatus, ThirdPartyDetails, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.inject.bind
import org.mockito.Mockito.when
import pages.editThirdParty.EditThirdPartyReferencePage
import pages.report.{ChooseEoriPage, NewEmailNotificationPage}
import play.api.inject.guice.GuiceApplicationBuilder
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
            ThirdPartyAccessPeriodSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true, false, "eori")(request, messages(application)).toString
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
            ThirdPartyReferenceSummary.detailsRow(Some("bar"), false, "thirdPartyEori")(messages(application)).get,
            ThirdPartyAccessPeriodSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true, false, "eori")(request, messages(application)).toString
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
            ThirdPartyReferenceSummary.detailsRow(Some("bar"), true, "eori")(messages(application)).get,
            ThirdPartyAccessPeriodSummary
              .detailsRow(thirdPartyDetails, true, "eori", emptyUserAnswers)(messages(application))
              .get,
            DataTypesSummary.detailsRow(Set("import"), true, "eori")(messages(application)).get,
            DataTheyCanViewSummary
              .detailsRow(thirdPartyDetails, true, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true, false, "eori")(request, messages(application)).toString
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
            ThirdPartyReferenceSummary.detailsRow(Some("bar"), false, "thirdPartyEori")(messages(application)).get,
            ThirdPartyAccessPeriodSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true, false, "eori")(request, messages(application)).toString
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
            ThirdPartyReferenceSummary.detailsRow(None, false, "thirdPartyEori")(messages(application)).get,
            ThirdPartyAccessPeriodSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get,
            DataTypesSummary.detailsRow(Set("import"), false, "thirdPartyEori")(messages(application)).get,
            DataTheyCanViewSummary
              .detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers)(messages(application))
              .get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "", true, false, "eori")(request, messages(application)).toString
      }
    }

    "must display confirm changes and cancel buttons when there are changes" in {

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
        .thenReturn(Future.successful(thirdPartyDetails))

      when(mockFrontendAppConfig.editThirdPartyEnabled).thenReturn(true)
      when(mockFrontendAppConfig.feedbackUrl(any(classOf[RequestHeader]))).thenReturn("http://localhost/feedback")

      val modifiedUserAnswers = UserAnswers("id")
        .set(pages.editThirdParty.EditThirdPartyReferencePage("thirdPartyEori"), "changedRef")
        .success
        .value

      val application = new GuiceApplicationBuilder()
        .overrides(
          bind[DataRetrievalOrCreateAction].toInstance(new CustomFakeDataRetrievalOrCreateAction(modifiedUserAnswers)),
          bind[IdentifierAction].to[FakeIdentifierAction],
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad("thirdPartyEori").url)
        val result  = route(application, request).value

        val content = contentAsString(result)

        status(result) mustEqual OK
        content must include(messages(application)("editThirdParty.confirmChanges"))
        content must include(messages(application)("editThirdParty.cancel"))
      }
    }

    "removeAnswersAndRedirect" - {

      "must redirect the user and delete userAnswers" in {
        val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

        when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
          .thenReturn(Future.successful(CompanyInformation("foo", ConsentStatus.Granted)))

        when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
          .thenReturn(
            Future.successful(
              ThirdPartyDetails(
                referenceName = Some("bar"),
                accessStartDate = LocalDate.of(2025, 1, 1),
                accessEndDate = None,
                dataTypes = Set("import"),
                dataStartDate = None,
                dataEndDate = None
              )
            )
          )

        val ua = emptyUserAnswers
          .set(EditThirdPartyReferencePage("thirdParty1"), "refTP1")
          .success
          .value
          .set(EditThirdPartyReferencePage("thirdParty2"), "refTP2")
          .success
          .value
          .set(NewEmailNotificationPage, "someEori")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.thirdparty.routes.ThirdPartyDetailsController.removeAnswersAndRedirect("thirdParty1").url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          val capturedAnswers = userAnswersCaptor.getValue
          capturedAnswers.get(EditThirdPartyReferencePage("thirdParty1")) mustBe None
          capturedAnswers.get(EditThirdPartyReferencePage("thirdParty2")) mustBe Some("refTP2")
          redirectLocation(result).value mustEqual controllers.thirdparty.routes.AuthorisedThirdPartiesController
            .onPageLoad()
            .url
        }
      }
    }

  }
}
