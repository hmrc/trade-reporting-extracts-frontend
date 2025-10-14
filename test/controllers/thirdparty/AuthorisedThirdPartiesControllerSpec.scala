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
import models.UserActiveStatus
import models.thirdparty.AuthorisedThirdPartiesViewModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import views.html.thirdparty.AuthorisedThirdPartiesView

import scala.concurrent.Future

class AuthorisedThirdPartiesControllerSpec extends SpecBase with MockitoSugar {

  "AuthorisedThirdPartiesController" - {

    "must return OK and the correct view for a GET when no authorised third parties" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getAuthorisedThirdParties(any())(any()))
        .thenReturn(Future.successful(Seq.empty[AuthorisedThirdPartiesViewModel]))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedThirdPartiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(
          request,
          messages(application)
        ).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include("You have not added any third parties yet")
      }
    }

    "must return OK and the correct view for a GET when authorised third parties exist" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      val thirdParty                        = Seq(
        AuthorisedThirdPartiesViewModel(
          eori = "GB123456789000",
          businessInfo = Some("Business Name"),
          referenceName = Some("Reference Name"),
          UserActiveStatus.Active
        ),
        AuthorisedThirdPartiesViewModel(
          eori = "GB987654321000",
          businessInfo = Some("Another Business Name"),
          referenceName = Some("Another Reference Name"),
          UserActiveStatus.Upcoming
        )
      )
      when(mockTradeReportingExtractsService.getAuthorisedThirdParties(any())(any()))
        .thenReturn(Future.successful(thirdParty))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedThirdPartiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(thirdParty)(
          request,
          messages(application)
        ).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include(
          "Manage third parties that can access your data"
        )
        document.text()                                        must include("GB123456789000")
        document.text()                                        must include("Business Name")
        document.text()                                        must include("Reference Name")
        document.getElementsByClass("govuk-tag--green").text() must include("Active")

        document.text()                                       must include("GB987654321000")
        document.text()                                       must include("Another Business Name")
        document.text()                                       must include("Another Reference Name")
        document.getElementsByClass("govuk-tag--blue").text() must include("Upcoming")
      }
    }

    "must return OK and the correct view for a GET when authorised third parties exist but show correctly when businessInfo and refName don't exist" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      val thirdParty                        = AuthorisedThirdPartiesViewModel(
        eori = "GB123456789000",
        businessInfo = None,
        referenceName = None,
        UserActiveStatus.Active
      )
      when(mockTradeReportingExtractsService.getAuthorisedThirdParties(any())(any()))
        .thenReturn(Future.successful(Seq(thirdParty)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedThirdPartiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq(thirdParty))(
          request,
          messages(application)
        ).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include(
          "Manage third parties that can access your data"
        )
        document.text()                                        must include("GB123456789000")
        document.text()                                        must include(
          "This business has not agreed to share their data. Contact them directly for more information."
        )
        document.text()                                        must include("Not applicable")
      }
    }
  }
}
