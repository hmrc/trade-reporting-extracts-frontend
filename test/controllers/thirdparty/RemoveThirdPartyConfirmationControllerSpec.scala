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
import models.{NotificationEmail, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.thirdparty.RemoveThirdPartyPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.thirdparty.RemoveThirdPartyConfirmationView
import play.api.inject.bind

import java.time.{Clock, Instant, LocalDateTime, ZoneId}
import scala.concurrent.Future

class RemoveThirdPartyConfirmationControllerSpec extends SpecBase {

  val mockSessionRepository: SessionRepository                         = mock[SessionRepository]
  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]

  "RemoveThirdPartyConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {


      val fixedInstant: Instant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneId.systemDefault())
      val userAnswers       = UserAnswers("id").set(RemoveThirdPartyPage, true).success.value
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      val notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now())

      when(mockTradeReportingExtractsService.removeThirdParty(any(), any())(any()))
        .thenReturn(Future.successful(Done))
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)
      when(mockTradeReportingExtractsService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(notificationEmail))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[Clock].toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.RemoveThirdPartyConfirmationController.onPageLoad("Eori").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveThirdPartyConfirmationView]

        status(result) mustEqual OK
      }
    }
  }
}
