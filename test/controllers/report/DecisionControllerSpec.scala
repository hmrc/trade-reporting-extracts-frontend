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
import forms.report.DecisionFormProvider
import navigation.{FakeReportNavigator, ReportNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.report.DecisionView

import scala.concurrent.Future

class DecisionControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new DecisionFormProvider()
  val form = formProvider()
  val onwardRoute = Call("GET", "/foo")

  "Decision Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DecisionController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DecisionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, routes.DecisionController.onSubmit())(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[ReportNavigator].toInstance(new FakeReportNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.DecisionController.onSubmit().url)
          .withFormUrlEncodedBody(("value", "import"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository, times(1)).set(any())
      }
    }

    "must return a Bad Request and errors when no value is selected" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.DecisionController.onSubmit().url)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[DecisionView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, routes.DecisionController.onSubmit())(request, messages(application)).toString
      }
    }
  }
}