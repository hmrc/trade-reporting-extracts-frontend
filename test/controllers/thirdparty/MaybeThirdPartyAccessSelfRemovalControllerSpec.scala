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

package controllers.thirdparty

import base.SpecBase
import controllers.routes
import forms.thirdparty.MaybeThirdPartyAccessSelfRemovalFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.thirdparty.MaybeThirdPartyAccessSelfRemovalPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.thirdparty.MaybeThirdPartyAccessSelfRemovalView

import scala.concurrent.Future

class MaybeThirdPartyAccessSelfRemovalControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new MaybeThirdPartyAccessSelfRemovalFormProvider()
  val form         = formProvider()

  lazy val maybeThirdPartyAccessSelfRemovalRoute =
    controllers.thirdparty.routes.MaybeThirdPartyAccessSelfRemovalController.onPageLoad("traderEori").url

  "MaybeThirdPartyAccessSelfRemoval Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, maybeThirdPartyAccessSelfRemovalRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaybeThirdPartyAccessSelfRemovalView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, "traderEori")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when true is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, maybeThirdPartyAccessSelfRemovalRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.thirdparty.routes.ThirdPartyAccessSelfRemovedController
          .onPageLoad("traderEori")
          .url
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(MaybeThirdPartyAccessSelfRemovalPage) mustBe Some(true)
      }
    }

    "must redirect to the next page when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, maybeThirdPartyAccessSelfRemovalRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.thirdparty.routes.AccountsAuthorityOverController
          .onPageLoad()
          .url
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(MaybeThirdPartyAccessSelfRemovalPage) mustBe None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, maybeThirdPartyAccessSelfRemovalRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MaybeThirdPartyAccessSelfRemovalView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "traderEori")(request, messages(application)).toString
      }
    }
  }
}
