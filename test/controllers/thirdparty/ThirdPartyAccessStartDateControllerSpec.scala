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
import forms.thirdparty.ThirdPartyAccessStartDateFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.thirdparty.{ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage}
import utils.json.OptionalLocalDateReads.*
import play.api.i18n.{Lang, Messages}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.DateTimeFormats
import views.html.thirdparty.ThirdPartyAccessStartDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class ThirdPartyAccessStartDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new ThirdPartyAccessStartDateFormProvider()
  private def form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val currentDate: String = LocalDate.now.format(DateTimeFormats.dateTimeHintFormat)

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val thirdPartyAccessStartDateRoute =
    controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(NormalMode).url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, thirdPartyAccessStartDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, thirdPartyAccessStartDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "ThirdPartyAccessStartDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[ThirdPartyAccessStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, currentDate)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ThirdPartyAccessStartDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[ThirdPartyAccessStartDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, currentDate)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must clear end date page value when new start date is after existing end date" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val userAnswers = emptyUserAnswers
        .set(ThirdPartyAccessStartDatePage, LocalDate.of(2023, 1, 1))
        .success
        .value
        .set(ThirdPartyAccessEndDatePage, Some(LocalDate.of(2023, 6, 1)))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ThirdPartyAccessEndDatePage) mustBe None
        capturedAnswers.get(ThirdPartyAccessStartDatePage) mustBe Some(LocalDate.now)
      }
    }

    "must not clear end date page value when new start date is before existing end date" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val userAnswers = emptyUserAnswers
        .set(ThirdPartyAccessStartDatePage, LocalDate.of(2023, 1, 1))
        .success
        .value
        .set(ThirdPartyAccessEndDatePage, Some(LocalDate.now.plusYears(1)))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ThirdPartyAccessEndDatePage) mustBe Some(Some(LocalDate.now.plusYears(1)))
        capturedAnswers.get(ThirdPartyAccessStartDatePage) mustBe Some(LocalDate.now)
      }
    }

    "must not clear end date page value when new start date is the same as existing end date" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val userAnswers = emptyUserAnswers
        .set(ThirdPartyAccessStartDatePage, LocalDate.of(2023, 1, 1))
        .success
        .value
        .set(ThirdPartyAccessEndDatePage, Some(LocalDate.now))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ThirdPartyAccessEndDatePage) mustBe Some(Some(LocalDate.now))
        capturedAnswers.get(ThirdPartyAccessStartDatePage) mustBe Some(LocalDate.now)
      }
    }

    "must submit successfully when end date access page hasn't been answered yet" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val userAnswers = emptyUserAnswers
        .set(ThirdPartyAccessStartDatePage, LocalDate.of(2023, 1, 1))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ThirdPartyAccessEndDatePage) mustBe None
        capturedAnswers.get(ThirdPartyAccessStartDatePage) mustBe Some(LocalDate.now)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, thirdPartyAccessStartDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ThirdPartyAccessStartDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, currentDate)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
