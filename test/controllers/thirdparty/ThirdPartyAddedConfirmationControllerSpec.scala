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
import config.FrontendAppConfig
import controllers.routes
import pages.thirdparty.EoriNumberPage
import play.api.test.FakeRequest
import play.api.inject.bind
import play.api.test.Helpers.*
import views.html.thirdparty.ThirdPartyAddedConfirmationView

import java.time.{Clock, Instant, ZoneId}

class ThirdPartyAddedConfirmationControllerSpec extends SpecBase {

  val fixedInstant: Instant = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneId.systemDefault())

  "ThirdPartyAddedConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(EoriNumberPage, "GB123456789000").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
        bind[Clock].toInstance(fixedClock)
      ).build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl
        val request = FakeRequest(GET, controllers.thirdparty.routes.ThirdPartyAddedConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ThirdPartyAddedConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          "GB123456789000",
          "5 May 2025",
          surveyUrl
        )(request, messages(application)).toString
      }
    }
  }
}
