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
import models.thirdparty.ThirdPartyRemovalMeta
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.DateTimeFormats
import views.html.thirdparty.RemoveThirdPartyConfirmationView

import java.time.{Clock, Instant, ZoneId}

class RemoveThirdPartyConfirmationControllerSpec extends SpecBase {

  "RemoveThirdPartyConfirmationController" - {

    "must return OK and render the correct view for a GET" in {
      val fixedInstant: Instant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

      val removalMeta = ThirdPartyRemovalMeta(
        eori = "GB123456789000",
        submittedAt = fixedInstant,
        notificationEmail = Some("notify@example.com")
      )

      val userAnswers = emptyUserAnswers.copy(
        submissionMeta = Some(Json.toJson(removalMeta).as[JsObject])
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val view = application.injector.instanceOf[RemoveThirdPartyConfirmationView]

        val request =
          FakeRequest(GET, controllers.thirdparty.routes.RemoveThirdPartyConfirmationController.onPageLoad.url)
        val result  = route(application, request).value

        val (expectedDate, expectedTime) =
          DateTimeFormats.instantToDateAndTime(fixedInstant, fixedClock)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          expectedDate,
          expectedTime,
          "GB123456789000",
          "notify@example.com"
        )(request, messages(application)).toString
      }
    }

    "return OK and render empty values when submissionMeta is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.thirdparty.routes.RemoveThirdPartyConfirmationController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("")
      }
    }
  }

}
