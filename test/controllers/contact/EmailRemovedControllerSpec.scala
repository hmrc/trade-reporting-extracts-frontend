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
import forms.additionalEmail.EmailRemovedFormProvider
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.additionalEmail.emailRemovedPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.contact.emailRemovedView

import scala.concurrent.Future

class EmailRemovedControllerSpec extends SpecBase with MockitoSugar {

  private val emailAddress         = "test@test.com"
  val formProvider                 = new EmailRemovedFormProvider()
  val form: Form[Boolean]          = formProvider()
  private lazy val onPageLoadRoute =
    controllers.contact.routes.EmailRemovedController.onPageLoad(emailAddress).url

  private lazy val onSubmitRoute =
    controllers.contact.routes.EmailRemovedController.onSubmit(emailAddress).url

  "EmailRemovedController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadRoute)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[emailRemovedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, emailAddress)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(emailRemovedPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoadRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[emailRemovedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, emailAddress)(request, messages(application)).toString
      }
    }

    "must redirect to EmailRemovedConfirmationController and call removeAddiotnalEmail when value = true" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockTradeService      = mock[TradeReportingExtractsService]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(mockTradeService.removeAddiotnalEmail(any(), any())(any()))
        .thenReturn(Future.successful(Done))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.contact.routes.EmailRemovedConfirmationController.onPageLoad(emailAddress).url

        verify(mockSessionRepository, times(1)).set(any())

        verify(mockTradeService, times(1))
          .removeAddiotnalEmail(any[String], eqTo(emailAddress))(any())
      }
    }

    "must redirect to COntact detils page when value = false" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockTradeService      = mock[TradeReportingExtractsService]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(false))
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, onSubmitRoute)
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.contact.routes.ContactDetailsController.onPageLoad().url
      }
    }
  }
}
