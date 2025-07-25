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
import config.FrontendAppConfig
import forms.report.ReportNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.ReportNamePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.ReportNameView

import scala.concurrent.Future

class ReportNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/request-customs-declaration-data/add-another-email")

  val formProvider       = new ReportNameFormProvider()
  val form: Form[String] = formProvider(
    "reportName.error.required",
    "reportName.error.length",
    "reportName.error.invalid"
  )

  lazy val reportNameRoute: String = controllers.report.routes.ReportNameController.onPageLoad(NormalMode).url

  "ReportName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reportNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportNameView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(form, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ReportNamePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, reportNameRoute)

        val view = application.injector.instanceOf[ReportNameView]

        val result = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe view(form.fill("answer"), NormalMode, false)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository            = mock[SessionRepository]
      val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
      when(mockAppConfig.notificationsEnabled).thenReturn(true)
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reportNameRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, reportNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReportNameView]

        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe view(boundForm, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, reportNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, reportNameRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
