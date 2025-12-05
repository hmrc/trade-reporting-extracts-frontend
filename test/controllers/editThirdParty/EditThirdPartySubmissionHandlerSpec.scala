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
import models.UserAnswers
import models.thirdparty.{DataTypes, EditThirdPartyRequest}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.*
import play.api.mvc.Result
import play.api.test.Helpers.*
import play.api.test.FakeRequest
import services.TradeReportingExtractsService

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

    val mockService = mock[TradeReportingExtractsService]

    "must call TradeReportingExtractsService and redirect on success" in {

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.successful(()))

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

      val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

      val result: Future[Result] =
        controller.submit(thirdPartyEori)(FakeRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.DashboardController.onPageLoad().url

      val captor = ArgumentCaptor.forClass(classOf[EditThirdPartyRequest])
      verify(mockService).editThirdPartyRequest(captor.capture())(any())

      val sent = captor.getValue
      sent.userEORI mustBe "GB123456789012"
      sent.thirdPartyEORI mustBe thirdPartyEori
      sent.accessStart.value mustBe accessStartInstant
      sent.accessEnd.value mustBe accessEndInstant
      sent.referenceName.value mustBe "Ref Name"
      sent.accessType.value must contain("IMPORT")

    }

    "must redirect to Dashboard when service fails" in {

      when(mockService.editThirdPartyRequest(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

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

      val controller = application.injector.instanceOf[EditThirdPartySubmissionHandler]

      val result = controller.submit(thirdPartyEori)(FakeRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.DashboardController.onPageLoad().url

    }
  }
}
