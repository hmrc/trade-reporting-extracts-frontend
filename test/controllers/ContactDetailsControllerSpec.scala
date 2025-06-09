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
import models.{AddressInformation, CompanyInformation, NotificationEmail, UserDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ContactDetailsView
import java.time.LocalDateTime

import scala.concurrent.Future

class ContactDetailsControllerSpec extends SpecBase {

  "ContactDetailsController" - {

    "must return OK and render the correct view for a GET request" in new Setup {

      running(application) {

        val userDetails = UserDetails(
          eori = eori,
          additionalEmails = Seq("test@example.com"),
          authorisedUsers = Seq.empty,
          companyInformation = companyInformation,
          notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now())
        )

        when(mockService.setupUser(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(userDetails))

        val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(companyInformation, eori)(request, messages(application)).toString
      }
    }
  }

  trait Setup {
    val mockService: TradeReportingExtractsService = mock[TradeReportingExtractsService]

    val companyInformation: CompanyInformation =
      CompanyInformation(
        name = "ABC Company",
        consent = "1",
        address = AddressInformation("XYZ Street", "ABC City", Some("G11 2ZZ"), "GB")
      )

    val eori: String = "GB123456789002"

    val application: Application = applicationBuilder()
      .overrides(
        inject.bind[TradeReportingExtractsService].toInstance(mockService)
      )
      .configure("features.new-agent-view-enabled" -> false)
      .build()
  }
}
