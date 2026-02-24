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

import base.SpecBase
import forms.additionalEmail.NewAdditionalEmailFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.additionalEmail.NewAdditionalEmailPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.contact.NewAdditionalEmailView

import scala.concurrent.Future

class NewAdditionalEmailControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new NewAdditionalEmailFormProvider()
  val emptyForm    = formProvider()
  val eoriNumber   = "GB123456789000"

  lazy val newAdditionalEmailRoute = controllers.contact.routes.NewAdditionalEmailController.onPageLoad().url

  "NewAdditionalEmail Controller" - {

    "must return OK and the correct view for a GET when no existing emails" in {
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(Seq.empty))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, newAdditionalEmailRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[NewAdditionalEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(emptyForm)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when existing emails are present" in {
      val existingEmails            = Seq("existing1@example.com", "existing2@example.com")
      val mockTradeReportingService = mock[TradeReportingExtractsService]
      val formWithExistingEmails    = formProvider(existingEmails)

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, newAdditionalEmailRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[NewAdditionalEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formWithExistingEmails)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val existingEmails            = Seq("existing@example.com")
      val previousAnswer            = "previous@example.com"
      val mockTradeReportingService = mock[TradeReportingExtractsService]
      val formWithExistingEmails    = formProvider(existingEmails)

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val userAnswersWithPreviousValue = emptyUserAnswers.set(NewAdditionalEmailPage, previousAnswer).get
      val application                  = applicationBuilder(userAnswers = Some(userAnswersWithPreviousValue))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, newAdditionalEmailRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[NewAdditionalEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formWithExistingEmails.fill(previousAnswer))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid unique email is submitted" in {
      val existingEmails            = Seq("existing@example.com")
      val newEmail                  = "new@example.com"
      val mockSessionRepository     = mock[SessionRepository]
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", newEmail))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.contact.routes.CheckAdditionalEmailController
          .onPageLoad()
          .url
      }
    }

    "must redirect to the next page when valid unique email is submitted (no previous user answers)" in {
      val existingEmails            = Seq("existing@example.com")
      val newEmail                  = "new@example.com"
      val mockSessionRepository     = mock[SessionRepository]
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))
      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", newEmail))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.contact.routes.CheckAdditionalEmailController
          .onPageLoad()
          .url
      }
    }
    "must handle email normalization correctly for duplicate checking" in {
      val existingEmails            = Seq("test@example.com")
      val emailWithSpaces           = "  test@example.com  " // This should be handled by the form validation
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", emailWithSpaces))

        val result = route(application, request).value

        // The validation should catch this as either invalid format or duplicate
        status(result) mustEqual BAD_REQUEST
      }
    }
    "must return a Bad Request and errors when duplicate email is submitted" in {
      val existingEmails            = Seq("existing@example.com", "another@example.com")
      val duplicateEmail            = "existing@example.com"
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", duplicateEmail))

        val boundForm = formProvider(existingEmails).bind(Map("value" -> duplicateEmail))
        val view      = application.injector.instanceOf[NewAdditionalEmailView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString

        boundForm.hasErrors mustBe true
        boundForm.errors.head.message mustEqual "newAdditionalEmail.error.alreadyAdded"
      }
    }

    "must return a Bad Request and errors when duplicate email is submitted with different case" in {
      val existingEmails            = Seq("existing@example.com")
      val duplicateEmail            = "EXISTING@EXAMPLE.COM"
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", duplicateEmail))

        val boundForm = formProvider(existingEmails).bind(Map("value" -> duplicateEmail))
        val view      = application.injector.instanceOf[NewAdditionalEmailView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString

        boundForm.hasErrors mustBe true
        boundForm.errors.head.message mustEqual "newAdditionalEmail.error.alreadyAdded"
      }
    }

    "must return a Bad Request and errors when empty email is submitted" in {
      val existingEmails            = Seq("existing@example.com")
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider(existingEmails).bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[NewAdditionalEmailView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString

        boundForm.hasErrors mustBe true
        boundForm.errors.head.message mustEqual "newAdditionalEmail.error.required"
      }
    }

    "must return a Bad Request and errors when invalid email format is submitted" in {
      val existingEmails            = Seq("existing@example.com")
      val invalidEmail              = "invalid-email"
      val mockTradeReportingService = mock[TradeReportingExtractsService]

      when(mockTradeReportingService.getAdditionalEmails(any())(any()))
        .thenReturn(Future.successful(existingEmails))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, newAdditionalEmailRoute)
          .withFormUrlEncodedBody(("value", invalidEmail))

        val boundForm = formProvider(existingEmails).bind(Map("value" -> invalidEmail))
        val view      = application.injector.instanceOf[NewAdditionalEmailView]
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString

        boundForm.hasErrors mustBe true
        boundForm.errors.head.message mustEqual "newAdditionalEmail.error.invalidFormat"
      }
    }
  }
}
