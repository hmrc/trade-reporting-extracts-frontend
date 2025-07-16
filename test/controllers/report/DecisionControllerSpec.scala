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
import forms.report.DecisionFormProvider
import models.report.Decision
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, FakeReportNavigator, Navigator, ReportNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.report.DecisionPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.DecisionView
import controllers.problem.routes
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Result}
import controllers.actions.BelowReportRequestLimitAction
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.test.Helpers.stubMessagesControllerComponents
import play.api.mvc.Results.Redirect
import controllers.problem.routes.TooManySubmissionsController

import scala.concurrent.Future

class DecisionControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/request-customs-declaration-data/which-eori")

  lazy val decisionRoute = controllers.report.routes.DecisionController.onPageLoad(NormalMode).url

  val formProvider = new DecisionFormProvider()
  val form         = formProvider()

  "Decision Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, decisionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DecisionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(DecisionPage, Decision.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, decisionRoute)

        val view = application.injector.instanceOf[DecisionView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Decision.values.head), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
      val mockSessionRepository            = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      val application                      =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
          )
          .build()
      when(mockAppConfig.thirdPartyEnabled).thenReturn(true)
      running(application) {
        val request =
          FakeRequest(POST, decisionRoute)
            .withFormUrlEncodedBody(("value", Decision.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, decisionRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DecisionView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, decisionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, decisionRoute)
            .withFormUrlEncodedBody(("value", Decision.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to TooManySubmissionsController for a GET when submission limit is reached" in {
      val mockAction = new BelowReportRequestLimitAction with MockitoSugar {
        override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] =
          Future.successful(Left(Redirect(TooManySubmissionsController.onPageLoad())))

        override protected def executionContext = global
      }

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[BelowReportRequestLimitAction].toInstance(mockAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, decisionRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TooManySubmissionsController.onPageLoad().url
      }
    }

    "must redirect to TooManySubmissionsController for a POST when submission limit is reached" in {
      val mockAction = new BelowReportRequestLimitAction with MockitoSugar {
        override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] =
          Future.successful(Left(Redirect(TooManySubmissionsController.onPageLoad())))

        override protected def executionContext = global
      }

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[BelowReportRequestLimitAction].toInstance(mockAction))
        .build()

      running(application) {
        val request = FakeRequest(POST, decisionRoute)
          .withFormUrlEncodedBody(("value", Decision.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TooManySubmissionsController.onPageLoad().url
      }
    }

  }
}
