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
import forms.editThirdParty.EditDataEndDateFormProvider
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.EditDataEndDatePage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import utils.DateTimeFormats
import utils.json.OptionalLocalDateReads.*
import views.html.editThirdParty.EditDataEndDateView

import java.time.LocalDate
import scala.concurrent.Future

class EditDataEndDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider           = new EditDataEndDateFormProvider()
  private def form(start: LocalDate) = formProvider(start)

  def onwardRoute = Call("GET", "/foo")

  private val currentLocalDate: LocalDate = LocalDate.of(2025, 1, 15)
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

  private val fixedValidAnswer: LocalDate = LocalDate.of(2025, 6, 15)
  private val otherDate: LocalDate        = LocalDate.of(2024, 12, 31)

  val thirdPartyEori = "GB123456789000"

  lazy val editRoute =
    controllers.editThirdParty.routes.EditDataEndDateController.onPageLoad(thirdPartyEori).url

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

  "EditDataEndDate Controller" - {

    "must return OK and the correct view for a GET when no prior data and service has start date" in {

      val mockTradeService = mock[TradeReportingExtractsService]

      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeService))
        .build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[EditDataEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form(currentLocalDate),
          thirdPartyEori,
          DateTimeFormats.dateFormatter(currentLocalDate)
        )(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId).set(EditDataEndDatePage(thirdPartyEori), Some(fixedValidAnswer)).success.value

      val mockTradeService = mock[TradeReportingExtractsService]

      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), None)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeService))
        .build()

      running(application) {
        val view = application.injector.instanceOf[EditDataEndDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form(currentLocalDate).fill(Some(fixedValidAnswer)),
          thirdPartyEori,
          DateTimeFormats.dateFormatter(currentLocalDate)
        )(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must show empty form on GET when service start is after service end" in {

      val serviceStart     = LocalDate.of(2025, 6, 2)
      val serviceEnd       = LocalDate.of(2025, 6, 1)
      val mockTradeService = mock[TradeReportingExtractsService]

      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(serviceStart), Some(serviceEnd))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeService))
        .build()

      running(application) {
        val view = application.injector.instanceOf[EditDataEndDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          form(serviceStart),
          thirdPartyEori,
          DateTimeFormats.dateFormatter(serviceStart)
        )(
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
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), None)))

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

    "must set end date when service had no previous end date" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), None)))

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
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe Some(Some(fixedValidAnswer))
      }
    }

    "must remove stored end date when submitted end equals service-provided end" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val serviceEnd = fixedValidAnswer

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), Some(serviceEnd))))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestFor(serviceEnd)).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue

        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe None
      }
    }

    "must set LocalDate.MAX when user selects 'No End Date' and service had previous end date" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val serviceEnd = fixedValidAnswer

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), Some(serviceEnd))))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, editRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue

        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe Some(Some(LocalDate.MAX))
      }
    }

    "must update end date when submitted end differs from service-provided end" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val serviceEnd = otherDate

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), Some(serviceEnd))))

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
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(EditDataEndDatePage(thirdPartyEori)) mustBe Some(Some(fixedValidAnswer))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockTradeService = mock[TradeReportingExtractsService]

      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), None)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeService))
        .build()

      val request =
        FakeRequest(POST, editRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form(currentLocalDate).bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EditDataEndDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          thirdPartyEori,
          DateTimeFormats.dateFormatter(currentLocalDate)
        )(
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
  }
}
