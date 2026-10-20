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
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.*
import play.api.test.*
import views.html.report.ReportGuidanceView
import models.{AlreadySubmittedFlag, NormalMode, NotificationEmail, UserAnswers}
import models.report.ReportRequestSection
import repositories.SessionRepository
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.reset
import play.api.inject.bind
import controllers.actions.*
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import services.TradeReportingExtractsService
import play.api.Application

import java.time.LocalDateTime

class ReportGuidanceControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  private val mockSessionRepository             = mock[SessionRepository]

  override def fakeApplication(): Application =
    applicationBuilder(
      useSharedFakeAction = true,
      useFakeDataRetrievalOrCreateAction = false
    )
      .configure("features.third-party" -> true)
      .overrides(
        bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRetrievalOrCreateAction].toInstance(new DataRetrievalOrCreateActionImpl(mockSessionRepository))
      )
      .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTradeReportingExtractsService, mockSessionRepository)
    when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
  }

  "ReportGuidanceController" - {

    "must return OK and correct view for initial page load when thirdPartyEnabled = true" in {
      val mockService = mockTradeReportingExtractsService
      when(mockService.getReportRequestLimitNumber(any())).thenReturn(Future.successful("25"))
      when(mockService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(NotificationEmail("test@email.com", LocalDateTime.now(), false)))

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[ReportGuidanceView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        NormalMode,
        "25",
        routes.ChooseEoriController.onPageLoad(NormalMode).url,
        "test@email.com"
      )(request, messages(application)).toString
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

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockService = mockTradeReportingExtractsService
      when(mockService.getReportRequestLimitNumber(any())).thenReturn(Future.successful("25"))
      when(mockService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(NotificationEmail("test@email.com", LocalDateTime.now(), false)))

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[ReportGuidanceView]
      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        NormalMode,
        "25",
        routes.ChooseEoriController.onPageLoad(NormalMode).url,
        "test@email.com"
      )(
        request,
        messages(application)
      ).toString
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

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockService = mockTradeReportingExtractsService
      when(mockService.getReportRequestLimitNumber(any())).thenReturn(Future.successful("25"))
      when(mockService.getNotificationEmail(any())(any()))
        .thenReturn(Future.successful(NotificationEmail("test@email.com", LocalDateTime.now(), false)))

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
      val result  = route(application, request).value
      val view    = application.injector.instanceOf[ReportGuidanceView]
      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        NormalMode,
        "25",
        routes.ChooseEoriController.onPageLoad(NormalMode).url,
        "test@email.com"
      )(
        request,
        messages(application)
      ).toString
    }

    "must redirect to navigation url when not initial or recovery/checkYourAnswers" in {
      val userAnswers = emptyUserAnswers
        .set(ReportRequestSection().sectionNavigation, "/foo")
        .success
        .value

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val mockService = mockTradeReportingExtractsService

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)
      val result  = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual ReportRequestSection().navigateTo(userAnswers)
    }
  }
}
