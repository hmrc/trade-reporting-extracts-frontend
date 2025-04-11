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
import models.{AddressInformation, CompanyInformation}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ContactDetailsView

import scala.concurrent.Future

class ContactDetailsControllerSpec extends SpecBase {

  "ContactDetails Controller" - {

    "must return OK and the correct view for a GET" in new Setup {

      running(application) {
        when(mockApiService.getCompanyInformation()(any[HeaderCarrier]))
          .thenReturn(Future.successful(companyInformation))

        val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(companyInformation, eori)(request, messages(application)).toString
      }
    }
  }

  trait Setup {
    val mockApiService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
    val companyInformation: CompanyInformation        =
      CompanyInformation("ABC Company", "1", AddressInformation("XYZ Street", "ABC City", Some("G11 2ZZ"), "GB"))
    val eori                                          = "GB123456789002"
    val application: Application                      = applicationBuilder()
      .overrides(
        inject.bind[TradeReportingExtractsService].toInstance(mockApiService)
      )
      .configure("features.new-agent-view-enabled" -> false)
      .build()
  }
}
