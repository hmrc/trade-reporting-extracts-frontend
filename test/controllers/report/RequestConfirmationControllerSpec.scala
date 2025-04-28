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

package controllers.report

import base.SpecBase
import models.UserAnswers
import models.report.EmailSelection
import pages.report.{EmailSelectionPage, NewEmailNotificationPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.report.RequestConfirmationView

class RequestConfirmationControllerSpec extends SpecBase {

  "RequestConfirmationController" - {

    "must return OK and the correct view when EmailSelectionPage is defined" in {
      val emailSelection = Seq(EmailSelection.Email1, EmailSelection.Email3)
      val newEmail       = "new.email@example.com"

      val userAnswers = UserAnswers("id")
        .set(EmailSelectionPage, EmailSelection.values.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, newEmail)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq("email1@example.com", "email2@example.com", newEmail))(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and an empty list when EmailSelectionPage is not defined" in {
      val userAnswers = UserAnswers("id")

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RequestConfirmationController.onPageLoad().url)
        val view    = application.injector.instanceOf[RequestConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(request, messages(application)).toString
      }
    }
  }
}
