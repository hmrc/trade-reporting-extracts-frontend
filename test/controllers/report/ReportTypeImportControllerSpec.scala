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

import base.AppPerSuiteSpecBase
import config.FrontendAppConfig
import forms.report.ReportTypeImportFormProvider
import models.report.ReportTypeImport
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.ReportTypeImportPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.ReportTypeImportView

import scala.concurrent.Future

class ReportTypeImportControllerSpec extends AppPerSuiteSpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/request-customs-declaration-data/date-range")

  lazy val reportTypeImportRoute: String =
    controllers.report.routes.ReportTypeImportController.onPageLoad(NormalMode).url

  val formProvider                      = new ReportTypeImportFormProvider()
  val form: Form[Set[ReportTypeImport]] = formProvider()

  private val mockSessionRepository = mock[SessionRepository]

  when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

  override def fakeApplication() =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "ReportTypeImport Controller" - {

    "must return OK and the correct view for a GET" in {

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request = FakeRequest(GET, reportTypeImportRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ReportTypeImportView]

      val config = application.injector.instanceOf[FrontendAppConfig]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, NormalMode, config.guidanceWhatsInTheReportUrl)(
        request,
        messages(application)
      ).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(ReportTypeImportPage, ReportTypeImport.values.toSet).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, reportTypeImportRoute)

      val view = application.injector.instanceOf[ReportTypeImportView]

      val result = route(application, request).value
      val config = application.injector.instanceOf[FrontendAppConfig]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        form.fill(ReportTypeImport.values.toSet),
        NormalMode,
        config.guidanceWhatsInTheReportUrl
      )(
        request,
        messages(application)
      ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, reportTypeImportRoute)
          .withFormUrlEncodedBody(("value[0]", ReportTypeImport.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, reportTypeImportRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view   = application.injector.instanceOf[ReportTypeImportView]
      val config = application.injector.instanceOf[FrontendAppConfig]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode, config.guidanceWhatsInTheReportUrl)(
        request,
        messages(application)
      ).toString
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request = FakeRequest(GET, reportTypeImportRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request =
        FakeRequest(POST, reportTypeImportRoute)
          .withFormUrlEncodedBody(("value[0]", ReportTypeImport.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
