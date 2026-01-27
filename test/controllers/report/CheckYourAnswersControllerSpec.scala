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
import controllers.report
import exceptions.NoAuthorisedUserFoundException
import models.{AlreadySubmittedFlag, SectionNavigation}
import models.report.{ChooseEori, Decision, ReportDateRange, ReportTypeImport}
import models.EoriRole
import pages.report._
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "CheckYourAnswers Controller" - {
    val sectionNav = SectionNavigation("reportRequestSection")

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(EoriRolePage, Set(EoriRole.Importer))
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .success
        .value
        .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
        .success
        .value
        .set(ReportNamePage, "Test Report")
        .success
        .value
        .set(MaybeAdditionalEmailPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, report.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must hide DecisionSummary when thirdPartyEori and dataTypes is only exports" in {
      val sectionNav     = SectionNavigation("reportRequestSection")
      val thirdPartyEori = "GB123456789000"
      val userAnswers    = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value
        .set(DecisionPage, Decision.Export)
        .success
        .value
        .set(pages.report.SelectThirdPartyEoriPage, thirdPartyEori)
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem))
        .success
        .value
        .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
        .success
        .value
        .set(CustomRequestStartDatePage, LocalDate.of(2025, 1, 1))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.of(2025, 1, 31))
        .success
        .value
        .set(ReportNamePage, "Test Report")
        .success
        .value
        .set(MaybeAdditionalEmailPage, false)
        .success
        .value

      val mockTradeReportingExtractsService = mock[services.TradeReportingExtractsService]
      val thirdPartyDetails                 = models.ThirdPartyDetails(
        referenceName = Some("Test Name"),
        accessStartDate = LocalDate.now(),
        accessEndDate = None,
        dataTypes = Set("exports"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetails))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[services.TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content must not include "Type of data to download"
        status(result) mustEqual OK
      }
    }

    "must hide DecisionSummary when thirdPartyEori and dataTypes is only imports" in {
      val sectionNav     = SectionNavigation("reportRequestSection")
      val thirdPartyEori = "GB123456789000"
      val userAnswers    = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(pages.report.SelectThirdPartyEoriPage, thirdPartyEori)
        .success
        .value
        .set(EoriRolePage, Set(EoriRole.Importer))
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .success
        .value
        .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
        .success
        .value
        .set(CustomRequestStartDatePage, LocalDate.of(2025, 1, 1))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.of(2025, 1, 31))
        .success
        .value
        .set(ReportNamePage, "Test Report")
        .success
        .value
        .set(MaybeAdditionalEmailPage, false)
        .success
        .value

      val mockTradeReportingExtractsService = mock[services.TradeReportingExtractsService]
      val thirdPartyDetails                 = models.ThirdPartyDetails(
        referenceName = Some("Test Name"),
        accessStartDate = LocalDate.now(),
        accessEndDate = None,
        dataTypes = Set("imports"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetails))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[services.TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content must not include "Type of data to download"
        status(result) mustEqual OK
      }
    }

    "must show DecisionSummary when thirdPartyEori and dataTypes is both imports and exports" in {
      val sectionNav     = SectionNavigation("reportRequestSection")
      val thirdPartyEori = "GB123456789000"
      val userAnswers    = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(pages.report.SelectThirdPartyEoriPage, thirdPartyEori)
        .success
        .value
        .set(EoriRolePage, Set(EoriRole.Importer))
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .success
        .value
        .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
        .success
        .value
        .set(CustomRequestStartDatePage, LocalDate.of(2025, 1, 1))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.of(2025, 1, 31))
        .success
        .value
        .set(ReportNamePage, "Test Report")
        .success
        .value
        .set(MaybeAdditionalEmailPage, false)
        .success
        .value

      val mockTradeReportingExtractsService = mock[services.TradeReportingExtractsService]
      val thirdPartyDetails                 = models.ThirdPartyDetails(
        referenceName = Some("Test Name"),
        accessStartDate = LocalDate.now(),
        accessEndDate = None,
        dataTypes = Set("imports", "exports"),
        dataStartDate = None,
        dataEndDate = None
      )

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetails))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[services.TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content must include("Type of data to download")
        status(result) mustEqual OK
      }
    }

    "must redirect to RequestNotCompletedController when NoAuthorisedUserFoundException is thrown" in {
      val sectionNav     = SectionNavigation("reportRequestSection")
      val thirdPartyEori = "GB123456789000"
      val userAnswers    = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(DecisionPage, Decision.Export)
        .success
        .value
        .set(pages.report.SelectThirdPartyEoriPage, thirdPartyEori)
        .success
        .value

      val mockTradeReportingExtractsService = mock[services.TradeReportingExtractsService]

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.failed(new NoAuthorisedUserFoundException("No authorised user found for third party EORI:")))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[services.TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.report.routes.RequestNotCompletedController
          .onPageLoad(thirdPartyEori)
          .url
      }
    }

    "must redirect to ReportRequestIssueController when validation fails for incomplete user answers" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val incompleteUserAnswers = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(DecisionPage, Decision.Import)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(incompleteUserAnswers))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.ReportRequestIssueController
          .onPageLoad()
          .url

        // Verify that AlreadySubmittedFlag was set and session was updated
        verify(mockSessionRepository).set(argThat { (userAnswers: models.UserAnswers) =>
          userAnswers.get(AlreadySubmittedFlag()).contains(true)
        })
      }
    }
    "must clear all report request answers and set AlreadySubmittedFlag when validation fails" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswersWithData = emptyUserAnswers
        .set(sectionNav, "/request-customs-declaration-data/check-your-answers")
        .success
        .value
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(EoriRolePage, Set(EoriRole.Importer))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithData))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.CheckYourAnswersController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.ReportRequestIssueController
          .onPageLoad()
          .url

        // Verify that the session repository was called to set updated answers
        verify(mockSessionRepository).set(argThat { (updatedAnswers: models.UserAnswers) =>
          updatedAnswers.get(AlreadySubmittedFlag()).contains(true) &&
          updatedAnswers.get(DecisionPage).isEmpty &&
          updatedAnswers.get(EoriRolePage).isEmpty
        })
      }
    }
  }
}
