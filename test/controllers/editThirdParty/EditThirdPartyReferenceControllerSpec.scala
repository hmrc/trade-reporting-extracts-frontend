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

package controllers.editThirdParty

import base.SpecBase
import forms.editThirdParty.EditThirdPartyReferenceFormProvider
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.EditThirdPartyReferencePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.editThirdParty.EditThirdPartyReferenceView

import java.time.LocalDate
import scala.concurrent.Future

class EditThirdPartyReferenceControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute: Call = Call("GET", "/foo")

  private lazy val editThirdPartyReferenceRoute =
    controllers.editThirdParty.routes.EditThirdPartyReferenceController.onPageLoad("thirdPartyEori").url

  private val formProvider = new EditThirdPartyReferenceFormProvider()
  private val form         = formProvider()

  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  private val mockSessionRepository             = mock[SessionRepository]

  private val thirdPartyDetailsResponse = ThirdPartyDetails(
    referenceName = Some("TestingReport"),
    accessStartDate = LocalDate.now(),
    accessEndDate = None,
    dataTypes = Set("imports", "exports"),
    dataStartDate = None,
    dataEndDate = None
  )

  "EditThirdPartyReferenceController" - {

    "must return OK and the correct view for a GET" in {
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, editThirdPartyReferenceRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[EditThirdPartyReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("TestingReport"), "thirdPartyEori")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(EditThirdPartyReferencePage("thirdPartyEori"), "answer")
        .success
        .value

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, editThirdPartyReferenceRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[EditThirdPartyReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), "thirdPartyEori")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyReferenceRoute)
          .withFormUrlEncodedBody("value" -> "NewAnswer")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must set a user answer when submitted value differs from original reference name" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyReferenceRoute)
          .withFormUrlEncodedBody("value" -> "DifferentAnswer")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditThirdPartyReferencePage("thirdPartyEori")) mustBe Some("DifferentAnswer")
      }
    }

    "must remove the user answer when submitted value matches original reference name" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswers.set(EditThirdPartyReferencePage("thirdPartyEori"), "TestingReport").success.value)
      )
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyReferenceRoute)
          .withFormUrlEncodedBody("value" -> "TestingReport")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditThirdPartyReferencePage("thirdPartyEori")) mustBe None
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyReferenceRoute)
          .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[EditThirdPartyReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "thirdPartyEori")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, editThirdPartyReferenceRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyReferenceRoute)
          .withFormUrlEncodedBody("value" -> "answer")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
