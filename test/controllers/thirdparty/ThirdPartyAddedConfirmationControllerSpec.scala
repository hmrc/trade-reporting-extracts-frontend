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

package controllers.thirdparty

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.CompanyInformation
import models.ConsentStatus.Granted
import models.thirdparty.{ConfirmEori, DataTypes, DeclarationDate, ThirdPartyAddedConfirmation, ThirdPartyRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.thirdparty.{ConfirmEoriPage, DataEndDatePage, DataStartDatePage, DataTypesPage, DeclarationDatePage, EoriNumberPage, ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage, ThirdPartyDataOwnerConsentPage, ThirdPartyReferencePage}
import play.api.test.FakeRequest
import play.api.inject.bind
import play.api.test.Helpers.{running, *}
import services.{AuditService, ThirdPartyService, TradeReportingExtractsService}
import views.html.thirdparty.ThirdPartyAddedConfirmationView

import java.time.LocalDate
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class ThirdPartyAddedConfirmationControllerSpec extends SpecBase {

  val fixedInstant: Instant                                            = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock                                                = Clock.fixed(fixedInstant, ZoneId.systemDefault())
  val mockThirdPartyService: ThirdPartyService                         = mock[ThirdPartyService]
  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
  val mockAuditService: AuditService                                   = mock[AuditService]

  "ThirdPartyAddedConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ConfirmEoriPage, ConfirmEori.Yes)
        .success
        .value
        .set(ThirdPartyReferencePage, "ref")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.of(2025, 1, 1))
        .success
        .value
        .set(ThirdPartyAccessEndDatePage, Some(LocalDate.of(2025, 1, 1)))
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Export))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.CustomDateRange)
        .success
        .value
        .set(DataStartDatePage, LocalDate.of(2025, 1, 1))
        .success
        .value
        .set(DataEndDatePage, Some(LocalDate.of(2025, 1, 1)))
        .success
        .value

      when(mockThirdPartyService.buildThirdPartyAddRequest(any(), any())).thenReturn(
        ThirdPartyRequest(
          userEORI = "GB987654321098",
          thirdPartyEORI = "GB123456123456",
          accessStart = Instant.parse("2025-09-09T00:00:00Z"),
          accessEnd = Some(Instant.parse("2025-09-09T10:59:38.334682780Z")),
          reportDateStart = Some(Instant.parse("2025-09-10T00:00:00Z")),
          reportDateEnd = Some(Instant.parse("2025-09-09T10:59:38.334716742Z")),
          accessType = Set("IMPORT", "EXPORT"),
          referenceName = Some("TestReport")
        )
      )
      when(mockTradeReportingExtractsService.createThirdPartyAddRequest(any())(any()))
        .thenReturn(Future.successful(ThirdPartyAddedConfirmation(thirdPartyEori = "GB123456123456")))
      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("name", Granted)))
      when(mockAuditService.auditThirdPartyAdded(any())(any())).thenReturn(Future.successful(()))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[Clock].toInstance(fixedClock),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[ThirdPartyService].toInstance(mockThirdPartyService),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl
        val request   =
          FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyAddedConfirmationController.onPageLoad().url)
        val result    = route(application, request).value
        val view      = application.injector.instanceOf[ThirdPartyAddedConfirmationView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          "GB123456123456",
          "5 May 2025",
          surveyUrl
        )(request, messages(application)).toString
        verify(mockAuditService, times(1)).auditThirdPartyAdded(any())(any())
        verify(mockTradeReportingExtractsService, times(1)).getCompanyInformation(any())(any())

      }
    }
  }
}
