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
import forms.report.ReportDateRangeFormProvider
import models.report.ReportDateRange
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.ReportDateRangePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.ReportDateRangeView
import java.time.{Clock, Instant, ZoneOffset}

import scala.concurrent.Future

class ReportDateRangeControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/request-customs-declaration-data/report-name")

  lazy val reportDateRangeRoute: String = controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode).url

  val formProvider                = new ReportDateRangeFormProvider()
  val form: Form[ReportDateRange] = formProvider("reportDateRange.error.required")

  val fixedInstant: Instant = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  "ReportDateRange Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val request = FakeRequest(GET, reportDateRangeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportDateRangeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          ("1 April 2025", "30 April 2025"),
          isMoreThanOneReport = false
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view/dates when last day of calendar month after T-2 current day for a GET" in {

      val fixedInstant = Instant.parse("2025-05-01T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val request = FakeRequest(GET, reportDateRangeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportDateRangeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          ("1 March 2025", "31 March 2025"),
          isMoreThanOneReport = false
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val request = FakeRequest(GET, reportDateRangeRoute)

        val view = application.injector.instanceOf[ReportDateRangeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(ReportDateRange.LastFullCalendarMonth),
          NormalMode,
          ("1 April 2025", "30 April 2025"),
          isMoreThanOneReport = false
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, reportDateRangeRoute)
            .withFormUrlEncodedBody(("value", ReportDateRange.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, reportDateRangeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ReportDateRangeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          ("1 April 2025", "30 April 2025"),
          isMoreThanOneReport = false
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, reportDateRangeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, reportDateRangeRoute)
            .withFormUrlEncodedBody(("value", ReportDateRange.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
