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
import forms.editThirdParty.EditDataStartDateFormProvider
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.{EditDataEndDatePage, EditDataStartDatePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import utils.DateTimeFormats
import utils.json.OptionalLocalDateReads.*
import views.html.editThirdParty.EditDataStartDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class EditDataStartDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new EditDataStartDateFormProvider()
  private def form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  private val currentLocalDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
  val currentDate: String                 = currentLocalDate.format(DateTimeFormats.dateTimeHintFormat)

  private def thirdPartyDetails(dataStart: Option[LocalDate], dataEnd: Option[LocalDate]) =
    ThirdPartyDetails(
      referenceName = Some("TestingReport"),
      accessStartDate = currentLocalDate,
      accessEndDate = None,
      dataTypes = Set("imports", "exports"),
      dataStartDate = dataStart,
      dataEndDate = dataEnd
    )

  private val fixedValidAnswer: LocalDate = LocalDate.of(2025, 1, 15)

  val thirdPartyEori = "GB123456789000"

  lazy val editRoute =
    controllers.editThirdParty.routes.EditDataStartDateController.onPageLoad(thirdPartyEori).url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, editRoute)

  def postRequestFor(d: LocalDate): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, editRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> d.getDayOfMonth.toString,
        "value.month" -> d.getMonthValue.toString,
        "value.year"  -> d.getYear.toString
      )

  "EditDataStartDate Controller" - {

    "must return OK and the correct view for a GET when no prior data and no service data" in {

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeService)
        )
        .build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[EditDataStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, thirdPartyEori, currentDate)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(EditDataStartDatePage(thirdPartyEori), fixedValidAnswer).success.value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeService)
        )
        .build()

      running(application) {
        val view = application.injector.instanceOf[EditDataStartDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(fixedValidAnswer), thirdPartyEori, currentDate)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view from service when user answers missing but service has date" in {

      val serviceStart     = LocalDate.of(2023, 6, 1)
      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(serviceStart), None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeService)
        )
        .build()

      running(application) {
        val view = application.injector.instanceOf[EditDataStartDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(serviceStart), thirdPartyEori, currentDate)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(fixedValidAnswer)).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must clear edit end date page value when new start date is after existing end date (from user answers)" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditDataStartDatePage(thirdPartyEori), LocalDate.of(2024, 1, 1))
        .success
        .value
        .set(EditDataEndDatePage(thirdPartyEori), Some(LocalDate.of(2024, 12, 31)))
        .success
        .value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(fixedValidAnswer)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe None
        capturedAnswers.get(EditDataStartDatePage(thirdPartyEori)) mustBe Some(fixedValidAnswer)
      }
    }

    "must clear edit end date page value when new start date is after existing end date (from service if not in user answers)" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditDataStartDatePage(thirdPartyEori), LocalDate.of(2024, 1, 1))
        .success
        .value

      val serviceEnd       = fixedValidAnswer.minusDays(1)
      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(LocalDate.of(2024, 1, 1)), Some(serviceEnd))))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(fixedValidAnswer)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe None
        capturedAnswers.get(EditDataStartDatePage(thirdPartyEori)) mustBe Some(fixedValidAnswer)
      }
    }

    "must not clear end date page value when new start date is before existing end date" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val futureEnd   = LocalDate.of(2026, 1, 1)
      val userAnswers = emptyUserAnswers
        .set(EditDataStartDatePage(thirdPartyEori), LocalDate.of(2024, 1, 1))
        .success
        .value
        .set(EditDataEndDatePage(thirdPartyEori), Some(futureEnd))
        .success
        .value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(fixedValidAnswer)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe Some(Some(futureEnd))
        capturedAnswers.get(EditDataStartDatePage(thirdPartyEori)) mustBe Some(fixedValidAnswer)
      }
    }

    "must not clear end date page value when new start is the same as existing" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val today       = LocalDate.of(2025, 2, 1)
      val userAnswers = emptyUserAnswers
        .set(EditDataStartDatePage(thirdPartyEori), LocalDate.of(2024, 1, 1))
        .success
        .value
        .set(EditDataEndDatePage(thirdPartyEori), Some(today))
        .success
        .value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(today)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe Some(Some(today))
        capturedAnswers.get(EditDataStartDatePage(thirdPartyEori)) mustBe Some(today)
      }
    }

    "must submit successfully when end date hasn't been answered yet" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditDataStartDatePage(thirdPartyEori), LocalDate.of(2024, 1, 1))
        .success
        .value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(fixedValidAnswer)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe None
        capturedAnswers.get(EditDataStartDatePage(thirdPartyEori)) mustBe Some(fixedValidAnswer)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeService)
        )
        .build()

      val request =
        FakeRequest(POST, editRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EditDataStartDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, thirdPartyEori, currentDate)(
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
        val result = route(application, postRequestFor(fixedValidAnswer)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must prefer user answers over service start date on GET" in {
      val svcStart         = LocalDate.of(2023, 6, 1)
      val userStart        = LocalDate.of(2024, 6, 1)
      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(svcStart), None)))

      val userAnswers = UserAnswers(userAnswersId).set(EditDataStartDatePage(thirdPartyEori), userStart).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeService))
        .build()

      running(application) {
        val result = route(application, getRequest()).value
        status(result) mustEqual OK
        val view   = application.injector.instanceOf[EditDataStartDateView]
        contentAsString(result) mustEqual view(form.fill(userStart), thirdPartyEori, currentDate)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must remove start date when new start equals existing start" in {
      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val existing    = LocalDate.of(2024, 1, 1)
      val existingEnd = LocalDate.of(2024, 12, 31)
      val userAnswers = emptyUserAnswers
        .set(EditDataStartDatePage(thirdPartyEori), existing)
        .success
        .value
        .set(EditDataEndDatePage(thirdPartyEori), Some(existingEnd))
        .success
        .value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(existing), Some(existingEnd))))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(existing)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue

        capturedAnswers.get(EditDataStartDatePage(thirdPartyEori)) mustBe None
      }
    }
  }
}
