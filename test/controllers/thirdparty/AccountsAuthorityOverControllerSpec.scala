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
import models.{ReportStatus, UserActiveStatus}
import models.thirdparty.AccountAuthorityOverViewModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import views.html.thirdparty.AccountsAuthorityOverView

import scala.concurrent.Future

class AccountsAuthorityOverControllerSpec extends SpecBase with MockitoSugar {

  "AccountsAuthorityOverController" - {

    "must return OK and the correct view for a GET when no accounts are available" in {
      val mockService = mock[TradeReportingExtractsService]
      when(mockService.getAccountsAuthorityOver(any())(any()))
        .thenReturn(Future.successful(Seq.empty[AccountAuthorityOverViewModel]))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AccountsAuthorityOverController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AccountsAuthorityOverView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(request, messages(application)).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include(
          "You do not have third-party access to any businesses"
        )
      }
    }

    "must return OK and the correct view for a GET when accounts exist" in {
      val mockService = mock[TradeReportingExtractsService]
      val accounts    = Seq(
        AccountAuthorityOverViewModel("GB123456789000", Some("Business Name"), Some(UserActiveStatus.Active)),
        AccountAuthorityOverViewModel("GB987654321000", Some("Another Business Name"), Some(UserActiveStatus.Upcoming))
      )
      when(mockService.getAccountsAuthorityOver(any())(any()))
        .thenReturn(Future.successful(accounts))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AccountsAuthorityOverController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[AccountsAuthorityOverView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(accounts)(request, messages(application)).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include("Businesses you have third-party access to")
        document.text()                                        must include("GB123456789000")
        document.text()                                        must include("Business Name")
        document.getElementsByClass("govuk-tag--green").text() must include("Active")

        document.text()                                       must include("GB987654321000")
        document.text()                                       must include("Another Business Name")
        document.getElementsByClass("govuk-tag--blue").text() must include("Upcoming")
      }
    }
  }
}
