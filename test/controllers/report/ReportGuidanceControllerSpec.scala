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
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.*
import play.api.test.*
import views.html.report.ReportGuidanceView
import models.{AlreadySubmittedFlag, NormalMode}
import models.report.ReportRequestSection
import repositories.SessionRepository
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import controllers.actions._
import models.UserAnswers
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.inject.guice.GuiceApplicationBuilder

class ReportGuidanceControllerSpec extends SpecBase with MockitoSugar {

  def appBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction]
      )

  "ReportGuidanceController" - {

    "must return OK and the correct view for a GET" in {

      val application = appBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportGuidanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode)(request, messages(application)).toString
      }
    }

    "must remove AlreadySubmittedFlag and return OK when navigation is JourneyRecoveryUrl" in {
      val userAnswers = emptyUserAnswers
        .set(
          ReportRequestSection().sectionNavigation,
          controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
        )
        .success
        .value
        .set(AlreadySubmittedFlag(), true)
        .success
        .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = appBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[DataRetrievalOrCreateAction].toInstance(new DataRetrievalOrCreateActionImpl(mockSessionRepository))
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ReportGuidanceView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode)(request, messages(application)).toString
      }
    }

    "must remove AlreadySubmittedFlag and return OK when navigation is checkYourAnswersUrl and AlreadySubmittedFlag is true" in {
      val userAnswers = emptyUserAnswers
        .set(
          ReportRequestSection().sectionNavigation,
          controllers.report.routes.CheckYourAnswersController.onPageLoad().url
        )
        .success
        .value
        .set(AlreadySubmittedFlag(), true)
        .success
        .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = appBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[DataRetrievalOrCreateAction].toInstance(new DataRetrievalOrCreateActionImpl(mockSessionRepository))
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ReportGuidanceView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to navigation url when not initial or recovery/checkYourAnswers" in {
      val userAnswers = emptyUserAnswers
        .set(ReportRequestSection().sectionNavigation, "/foo")
        .success
        .value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = appBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[DataRetrievalOrCreateAction].toInstance(new DataRetrievalOrCreateActionImpl(mockSessionRepository))
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ReportRequestSection().navigateTo(userAnswers)
      }
    }
  }
}
