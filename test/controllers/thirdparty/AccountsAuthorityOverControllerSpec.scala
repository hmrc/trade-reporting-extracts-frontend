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
import models.thirdparty.AccountAuthorityOverViewModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TradeReportingExtractsService
import views.html.thirdparty.AccountsAuthorityOverView

import scala.concurrent.Future

class AccountsAuthorityOverControllerSpec extends SpecBase with MockitoSugar {

  "AccountsAuthorityOver Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getAccountsAuthorityOver(any())(any()))
        .thenReturn(Future.successful(Seq.empty[AccountAuthorityOverViewModel]))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.AccountsAuthorityOverController.onPageLoad().url)

        val result = route(application, request).value

        val view       = application.injector.instanceOf[AccountsAuthorityOverView]
        val emptyModel = Seq.empty[AccountAuthorityOverViewModel]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(emptyModel)(request, messages(application)).toString
      }
    }
  }
}
