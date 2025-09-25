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

package controllers.problem
import base.SpecBase
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.*
import views.html.problem.AlreadyAddedThirdPartyView

class AlreadyAddedThirdPartyControllerSpec extends SpecBase {

  "AlreadySubmitted Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val eori    = "GB123456789000"
        val request = FakeRequest(GET, controllers.problem.routes.AlreadyAddedThirdPartyController.onPageLoad(eori).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AlreadyAddedThirdPartyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(eori)(request, messages(application)).toString
      }
    }
  }
}

// You will need to provide suitable FakeIdentifierAction and FakeDataRetrievalOrCreateAction for your test setup.
