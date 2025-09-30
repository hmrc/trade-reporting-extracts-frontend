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
import models.UserAnswers
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.thirdparty.MaybeThirdPartyAccessSelfRemovalPage
import play.api.i18n.Lang
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{AuditService, TradeReportingExtractsService}
import utils.DateTimeFormats.formattedSystemTime
import views.html.thirdparty.ThirdPartyAccessSelfRemovedView

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class ThirdPartyAccessSelfRemovedControllerSpec extends SpecBase with MockitoSugar {

  val mockSessionRepository: SessionRepository                         = mock[SessionRepository]
  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
  val mockAuditService                                                 = mock[AuditService]

  val fixedInstant: Instant = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.systemDefault())

  "ThirdPartyAccessSelfRemoved Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers       = UserAnswers("id").set(MaybeThirdPartyAccessSelfRemovalPage, true).success.value
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockTradeReportingExtractsService.selfRemoveThirdPartyAccess(any(), any())(any()))
        .thenReturn(Future.successful(Done))
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)
      when(mockAuditService.auditThirdPartySelfRemoval(any())(any())) thenReturn Future.successful(())

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Clock].toInstance(fixedClock),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.thirdparty.routes.ThirdPartyAccessSelfRemovedController.onPageLoad("traderEori").url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyAccessSelfRemovedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("5 May 2025", formattedSystemTime(fixedClock)(Lang("en")), "traderEori")(
          request,
          messages(application)
        ).toString
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(MaybeThirdPartyAccessSelfRemovalPage) mustBe None
        verify(mockAuditService, times(1)).auditThirdPartySelfRemoval(any())(any())
      }
    }
  }
}
