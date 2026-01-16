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

package controllers.editThirdParty

import base.SpecBase
import controllers.routes
import models.ThirdPartyDetails
import models.thirdparty.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalactic.Prettifier.default
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.*
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{AuditService, TradeReportingExtractsService}

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class EditThirdPartySubmissionHandlerSpec
    extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with SpecBase {

  "EditThirdPartySubmissionHandler.submit" - {

    val thirdPartyEori = "GB123456789000"

    val startDate = LocalDate.of(2025, 1, 1)
    val endDate   = LocalDate.of(2025, 12, 31)

    val accessStartInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    val accessEndInstant   = endDate.atStartOfDay().toInstant(ZoneOffset.UTC)

    "must call TradeReportingExtractsService and redirect on success and replace old details if edited" in {
      val mockService = mock[TradeReportingExtractsService]

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.successful(ThirdPartyAddedConfirmation("GB123456789000")))

      when(mockService.getThirdPartyDetails(any(), any())(any())).thenReturn(
        Future.successful(
          ThirdPartyDetails(
            None,
            LocalDate.of(2024, 1, 1),
            None,
            Set("EXPORT"),
            None,
            None
          )
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), startDate)
        .success
        .value
        .set(EditThirdPartyAccessEndDatePage(thirdPartyEori), endDate)
        .success
        .value
        .set(EditThirdPartyDataTypesPage(thirdPartyEori), Set(DataTypes.Import))
        .success
        .value
        .set(EditThirdPartyReferencePage(thirdPartyEori), "Ref Name")
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
        .build()

      running(application) {
        val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

        val result: Future[Result] =
          controller.submit(thirdPartyEori)(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.thirdparty.routes.ThirdPartyUpdatedConfirmationController
          .onPageLoad()
          .url

        val captor = ArgumentCaptor.forClass(classOf[ThirdPartyRequest])
        verify(mockService).editThirdPartyRequest(captor.capture())(any())

        val sent = captor.getValue
        sent.userEORI mustBe "GB123456789012"
        sent.thirdPartyEORI mustBe thirdPartyEori
        sent.accessStart mustBe accessStartInstant
        sent.accessEnd.value mustBe accessEndInstant
        sent.referenceName.value mustBe "Ref Name"
        sent.accessType must contain("IMPORT")
      }
    }

    "must not replace old details if not edited" in {
      val mockService = mock[TradeReportingExtractsService]

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.successful(()))

      when(mockService.getThirdPartyDetails(any(), any())(any())).thenReturn(
        Future.successful(
          ThirdPartyDetails(
            None,
            LocalDate.of(2024, 1, 1),
            Some(LocalDate.of(2024, 12, 31)),
            Set("EXPORT"),
            None,
            None
          )
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyReferencePage(thirdPartyEori), "Ref Name")
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
        .build()

      running(application) {
        val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

        val result: Future[Result] =
          controller.submit(thirdPartyEori)(FakeRequest())

        result.futureValue

        val captor = ArgumentCaptor.forClass(classOf[ThirdPartyRequest])
        verify(mockService).editThirdPartyRequest(captor.capture())(any())

        val sent = captor.getValue
        sent.userEORI mustBe "GB123456789012"
        sent.thirdPartyEORI mustBe thirdPartyEori
        sent.accessStart mustBe LocalDate.of(2024, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
        sent.accessEnd mustBe Some(LocalDate.of(2024, 12, 31).atStartOfDay().toInstant(ZoneOffset.UTC))
        sent.referenceName.value mustBe "Ref Name"
        sent.reportDateStart mustBe None
        sent.reportDateEnd mustBe None
        sent.accessType must contain("EXPORT")
      }
    }

    "must handle end date of LocalDate.MAX as no end date" in {
      val mockService = mock[TradeReportingExtractsService]

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.successful(()))

      when(mockService.getThirdPartyDetails(any(), any())(any())).thenReturn(
        Future.successful(
          ThirdPartyDetails(
            None,
            LocalDate.of(2024, 1, 1),
            Some(LocalDate.of(2024, 12, 31)),
            Set("EXPORT"),
            None,
            None
          )
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), startDate)
        .success
        .value
        .set(EditThirdPartyAccessEndDatePage(thirdPartyEori), LocalDate.MAX)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
        .build()

      running(application) {
        val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

        val result: Future[Result] =
          controller.submit(thirdPartyEori)(FakeRequest())

        result.futureValue

        val captor = ArgumentCaptor.forClass(classOf[ThirdPartyRequest])
        verify(mockService).editThirdPartyRequest(captor.capture())(any())

        val sent = captor.getValue
        sent.accessEnd mustBe None
      }
    }

    "must redirect to Dashboard when service fails" in {
      val mockService = mock[TradeReportingExtractsService]

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      when(mockService.getThirdPartyDetails(any(), any())(any())).thenReturn(
        Future.successful(
          ThirdPartyDetails(
            None,
            LocalDate.of(2024, 1, 1),
            Some(LocalDate.of(2024, 12, 31)),
            Set("EXPORT"),
            None,
            None
          )
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), startDate)
        .success
        .value
        .set(EditThirdPartyAccessEndDatePage(thirdPartyEori), endDate)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
        .build()

      running(application) {
        val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

        val result = controller.submit(thirdPartyEori)(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.DashboardController.onPageLoad().url
      }
    }

    "must call audit service with correct event when third party details are updated" in {
      val mockService      = mock[TradeReportingExtractsService]
      val mockAuditService = mock[AuditService]

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.successful(ThirdPartyAddedConfirmation("GB123456789000")))

      when(mockAuditService.auditThirdPartyUpdated(any())(any()))
        .thenReturn(Future.successful(()))

      when(mockService.getThirdPartyDetails(any(), any())(any())).thenReturn(
        Future.successful(
          ThirdPartyDetails(
            None,
            LocalDate.of(2024, 1, 1),
            Some(LocalDate.of(2024, 12, 31)),
            Set("EXPORT"),
            Some(LocalDate.of(2024, 6, 1)),
            Some(LocalDate.of(2024, 11, 30))
          )
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), startDate)
        .success
        .value
        .set(EditThirdPartyAccessEndDatePage(thirdPartyEori), endDate)
        .success
        .value
        .set(EditThirdPartyDataTypesPage(thirdPartyEori), Set(DataTypes.Import, DataTypes.Export))
        .success
        .value
        .set(EditThirdPartyReferencePage(thirdPartyEori), "Updated Reference")
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockService),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

        val result: Future[Result] = controller.submit(thirdPartyEori)(FakeRequest())

        status(result) mustBe SEE_OTHER

        val auditCaptor = ArgumentCaptor.forClass(classOf[ThirdPartyUpdatedEvent])
        verify(mockAuditService).auditThirdPartyUpdated(auditCaptor.capture())(any())

        val auditEvent = auditCaptor.getValue
        auditEvent.requesterEori mustBe "GB123456789012"
        auditEvent.thirdPartyEori mustBe thirdPartyEori

        auditEvent.updatesToThirdPartyData must contain(DataUpdate("accessType", "export", "import, export"))
        auditEvent.updatesToThirdPartyData must contain(DataUpdate("referenceName", "", "Updated Reference"))
        auditEvent.updatesToThirdPartyData must contain(
          DataUpdate("thirdPartyAccessStart", "2024-01-01T00:00:00Z", "2025-01-01T00:00:00Z")
        )
      }
    }

    "must handle audit service failure gracefully without affecting the edit operation" in {
      val mockService      = mock[TradeReportingExtractsService]
      val mockAuditService = mock[AuditService]

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.successful(ThirdPartyAddedConfirmation("GB123456789000")))

      when(mockAuditService.auditThirdPartyUpdated(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("Audit service failed")))

      when(mockService.getThirdPartyDetails(any(), any())(any())).thenReturn(
        Future.successful(
          ThirdPartyDetails(
            None,
            LocalDate.of(2024, 1, 1),
            None,
            Set("EXPORT"),
            None,
            None
          )
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyReferencePage(thirdPartyEori), "New Reference")
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockService),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

        val result: Future[Result] = controller.submit(thirdPartyEori)(FakeRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.thirdparty.routes.ThirdPartyUpdatedConfirmationController
          .onPageLoad()
          .url
        verify(mockAuditService).auditThirdPartyUpdated(any())(any())
        verify(mockService).editThirdPartyRequest(any())(any())
      }
    }
  }
}
