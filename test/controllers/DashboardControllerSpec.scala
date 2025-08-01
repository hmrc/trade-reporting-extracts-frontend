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
import base.TestConstants.testEori
import models.{CompanyInformation, NotificationEmail, UserDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import views.html.DashboardView

import java.time.LocalDateTime
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with MockitoSugar {
  val companyInformation: CompanyInformation =
    CompanyInformation("Test Company", "1")
  val userDetails: UserDetails               = UserDetails(
    eori = testEori,
    additionalEmails = Seq.empty,
    authorisedUsers = Seq.empty,
    companyInformation = companyInformation,
    notificationEmail = NotificationEmail("test@test.com", LocalDateTime.now)
  )

  trait Setup {
    val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
    when(mockTradeReportingExtractsService.setupUser(any[String])(any)) thenReturn Future.successful(userDetails)
  }

  "Dashboard Controller" - {

    "must return OK and the correct view for a GET" in new Setup {

      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK

      }
    }

    "must show the notifications card when notificationsEnabled is true" in new Setup {
      val application: Application = applicationBuilder()
        .configure(
          "features.notifications" -> true,
          "features.third-party"   -> false,
          "allowedEoris"           -> Seq(testEori)
        )
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)

        content.contains("Notifications") mustBe true
      }
    }

    "must not show the notifications card when notificationsEnabled is false" in new Setup {
      val application: Application = applicationBuilder()
        .configure(
          "features.notifications" -> false,
          "features.third-party"   -> false
        )
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content.contains("Notifications") mustBe false
      }
    }

    "must show the third party card when thirdPartyEnabled is true" in new Setup {
      val application: Application = applicationBuilder()
        .configure(
          "features.notifications" -> false,
          "features.third-party"   -> true
        )
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content.contains("Third-party access") mustBe true
      }
    }

    "must not show the third party card when thirdPartyEnabled is false" in new Setup {
      val application: Application = applicationBuilder()
        .configure(
          "features.notifications" -> false,
          "features.third-party"   -> false
        )
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content.contains("Third-party access") mustBe false
      }
    }
  }
}
