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

import base.AppPerSuiteSpecBase
import forms.editThirdParty.EditThirdPartyDataTypesFormProvider
import models.thirdparty.DataTypes
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import pages.editThirdParty.EditThirdPartyDataTypesPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.editThirdParty.EditThirdPartyDataTypesView

import java.time.LocalDate
import scala.concurrent.Future

class EditThirdPartyDataTypesControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  private val mockSessionRepository             = mock[SessionRepository]
  override def fakeApplication()                =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
      )
      .build()
  override protected def beforeEach(): Unit     = {
    super.beforeEach()
    org.mockito.Mockito.reset(mockTradeReportingExtractsService, mockSessionRepository)
    when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
      .thenReturn(Future.failed(new RuntimeException("No third party details found")))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  def onwardRoute = Call("GET", "/foo")

  lazy val editThirdPartyDataTypesRoute =
    controllers.editThirdParty.routes.EditThirdPartyDataTypesController.onPageLoad("thirdPartyEori").url

  val formProvider              = new EditThirdPartyDataTypesFormProvider()
  val form                      = formProvider()
  val thirdPartyDetailsResponse = ThirdPartyDetails(None, LocalDate.now(), None, Set("imports", "exports"), None, None)

  "EditThirdPartyDataTypes Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request = FakeRequest(GET, editThirdPartyDataTypesRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[EditThirdPartyDataTypesView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form.fill(Set(DataTypes.Import, DataTypes.Export)), "thirdPartyEori")(
        request,
        messages(application)
      ).toString
    }

    "must recover and redirect to dashboard when no third party details found for a GET" in {

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request = FakeRequest(GET, editThirdPartyDataTypesRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad().url
    }

    "must populate the view correctly on a GET when the question has previously been answered, should take precedent over previous relationship details" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(EditThirdPartyDataTypesPage("thirdPartyEori"), Set(DataTypes.values.head))
        .success
        .value

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, editThirdPartyDataTypesRoute)

      val view = application.injector.instanceOf[EditThirdPartyDataTypesView]

      val result = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form.fill(Set(DataTypes.values.head)), "thirdPartyEori")(
        request,
        messages(application)
      ).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, editThirdPartyDataTypesRoute)
          .withFormUrlEncodedBody(("value[0]", DataTypes.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
    }

    "must set a user answer when answer different to previous relationship details" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, editThirdPartyDataTypesRoute)
          .withFormUrlEncodedBody(("value[0]", DataTypes.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(EditThirdPartyDataTypesPage("thirdPartyEori")) mustBe Some(Set(DataTypes.values.head))
    }

    "must not create a user answer if answer is the same as previous relationship details and clean previous answers" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      setUserAnswers(
        Some(
          emptyUserAnswers
            .set(EditThirdPartyDataTypesPage("thirdPartyEori"), Set(DataTypes.Import))
            .success
            .value
        )
      )
      val application = app

      val request =
        FakeRequest(POST, editThirdPartyDataTypesRoute)
          .withFormUrlEncodedBody(
            "value[0]" -> DataTypes.Import.toString,
            "value[1]" -> DataTypes.Export.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(EditThirdPartyDataTypesPage("thirdPartyEori")) mustBe None
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, editThirdPartyDataTypesRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[EditThirdPartyDataTypesView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, "thirdPartyEori")(request, messages(application)).toString
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request = FakeRequest(GET, editThirdPartyDataTypesRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request =
        FakeRequest(POST, editThirdPartyDataTypesRoute)
          .withFormUrlEncodedBody(("value", DataTypes.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
