/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.contact

import base.AppPerSuiteSpecBase
import forms.additionalEmail.CheckAdditionalEmailFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import pages.additionalEmail.NewAdditionalEmailPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.contact.CheckAdditionalEmailView

import scala.concurrent.Future

class CheckAdditionalEmailControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val mockSessionRepository         = mock[SessionRepository]
  private val mockTradeReportingService     = mock[TradeReportingExtractsService]
  override def fakeApplication()            =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
      )
      .build()
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    org.mockito.Mockito.reset(mockSessionRepository, mockTradeReportingService)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  val formProvider = new CheckAdditionalEmailFormProvider()
  val form         = formProvider()
  val testEmail    = "test@example.com"

  lazy val checkAdditionalEmailRoute = controllers.contact.routes.CheckAdditionalEmailController.onPageLoad().url

  "CheckAdditionalEmail Controller" - {

    "must return OK and the correct view for a GET with email address from NewAdditionalEmailPage" in {

      val userAnswers = UserAnswers(userAnswersId).set(NewAdditionalEmailPage, testEmail).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, checkAdditionalEmailRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckAdditionalEmailView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, testEmail)(request, messages(application)).toString
    }

    "must redirect to  ContactDetails page when user answers doesn't have email address" in {

      val userAnswers = emptyUserAnswers

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, checkAdditionalEmailRoute)

      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.contact.routes.ContactDetailsController
        .onPageLoad()
        .url
    }

    "must redirect to AdditionalEmailAddedController when user confirms email (Yes)" in {
      when(mockTradeReportingService.addAdditionalEmail(any(), any())(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = UserAnswers(userAnswersId).set(NewAdditionalEmailPage, testEmail).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, checkAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.contact.routes.AdditionalEmailAddedController
        .onPageLoad(testEmail)
        .url

      verify(mockTradeReportingService).addAdditionalEmail(any(), any())(any())
      verify(mockSessionRepository).set(any())
    }

    "must redirect to NewAdditionalEmailController when user rejects email (No)" in {
      val userAnswers = UserAnswers(userAnswersId).set(NewAdditionalEmailPage, testEmail).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, checkAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.contact.routes.NewAdditionalEmailController
        .onPageLoad()
        .url

      verify(mockTradeReportingService, never()).addAdditionalEmail(any(), any())(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers(userAnswersId).set(NewAdditionalEmailPage, testEmail).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, checkAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      val view      = application.injector.instanceOf[CheckAdditionalEmailView]
      val boundForm = form.bind(Map("value" -> ""))

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, testEmail)(request, messages(application)).toString
    }

    "must handle service failure when adding additional email" in {
      when(mockTradeReportingService.addAdditionalEmail(any(), any())(any())).thenReturn(Future.successful(false))

      val userAnswers = UserAnswers(userAnswersId).set(NewAdditionalEmailPage, testEmail).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, checkAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      // Should throw RuntimeException for failed email addition
      whenReady(result.failed) { exception =>
        exception.getMessage mustEqual "Failed to add additional email"
      }
    }
  }
}
