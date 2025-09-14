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
import models.{CompanyInformation, ConsentStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.thirdparty.{EoriNumberPage, ThirdPartyReferencePage}
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import viewmodels.checkAnswers.thirdparty.{BusinessInfoSummary, EoriNumberSummary, ThirdPartyReferenceSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.thirdparty.AddThirdPartyCheckYourAnswersView

import scala.concurrent.Future

class AddThirdPartyCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  "AddThirdPartyCheckYourAnswers Controller" - {

    val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

    "must return OK and the correct view for a GET when business consent given" in {

      val userAnswers = emptyUserAnswers
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("businessInfo", ConsentStatus.Granted)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            inject.bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.AddThirdPartyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddThirdPartyCheckYourAnswersView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.checkYourAnswersRow(userAnswers)(messages(application)).get,
            BusinessInfoSummary.row("businessInfo")(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when business consent not given" in {

      val userAnswers = emptyUserAnswers
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyReferencePage, "ref")
        .success
        .value

      when(mockTradeReportingExtractsService.getCompanyInformation(any())(any()))
        .thenReturn(Future.successful(CompanyInformation("businessInfo", ConsentStatus.Denied)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            inject.bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.AddThirdPartyCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddThirdPartyCheckYourAnswersView]

        val list = SummaryListViewModel(
          Seq(
            EoriNumberSummary.checkYourAnswersRow(userAnswers)(messages(application)).get,
            ThirdPartyReferenceSummary.checkYourAnswersRow(userAnswers)(messages(application)).get
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }
  }
}
