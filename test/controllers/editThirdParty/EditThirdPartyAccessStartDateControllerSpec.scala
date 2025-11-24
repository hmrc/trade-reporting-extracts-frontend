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

import java.time.{Clock, Instant, LocalDate, ZoneId, ZoneOffset}
import base.SpecBase
import forms.EditThirdPartyAccessStartDateFormProvider
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.EditThirdPartyAccessStartDatePage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import utils.DateTimeFormats
import views.html.editThirdParty.EditThirdPartyAccessStartDateView

import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class EditThirdPartyAccessStartDateControllerSpec extends SpecBase with MockitoSugar {
  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)

  private implicit val messages: Messages = stubMessages()
  private val stubClock: Clock            = Clock.fixed(instant, ZoneId.systemDefault)

  private val formProvider = new EditThirdPartyAccessStartDateFormProvider()
  private def form         = formProvider()

  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  private val mockSessionRepository             = mock[SessionRepository]

  val currentDate: LocalDate = LocalDate.now(stubClock.getZone)

  private val thirdPartyDetailsResponse = ThirdPartyDetails(
    referenceName = Some("TestingReport"),
    accessStartDate = currentDate,
    accessEndDate = None,
    dataTypes = Set("imports", "exports"),
    dataStartDate = None,
    dataEndDate = None
  )

  private val thirdPartyEori                    = "thirdPartyEori"
  private val submittedDifferentDate: LocalDate = currentDate.plusDays(2)

  def onwardRoute: Call = Call("GET", "/request-customs-declaration-data/editThirdPartyAccessEnd/thirdPartyEori")

  private val currentDateFormatted: String = currentDate.format(DateTimeFormats.dateTimeHintFormat)

  lazy val editThirdPartyAccessStartDateRoute: String =
    controllers.editThirdParty.routes.EditThirdPartyAccessStartDateController.onPageLoad(thirdPartyEori).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, editThirdPartyAccessStartDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, editThirdPartyAccessStartDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> currentDate.getDayOfMonth.toString,
        "value.month" -> currentDate.getMonthValue.toString,
        "value.year"  -> currentDate.getYear.toString
      )

  "EditThirdPartyAccessStartDate Controller" - {

    "must return OK and the correct view for a GET" in {
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, editThirdPartyAccessStartDateRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[EditThirdPartyAccessStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(currentDate), thirdPartyEori, currentDateFormatted)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val view    = application.injector.instanceOf[EditThirdPartyAccessStartDateView]
        val request = FakeRequest(GET, editThirdPartyAccessStartDateRoute)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(currentDate), thirdPartyEori, currentDateFormatted)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, editThirdPartyAccessStartDateRoute)
        val result  = route(application, request).value
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

    "must set a user answer when submitted value differs from original start date" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyAccessStartDateRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> submittedDifferentDate.getDayOfMonth.toString,
            "value.month" -> submittedDifferentDate.getMonthValue.toString,
            "value.year"  -> submittedDifferentDate.getYear.toString
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val captured = userAnswersCaptor.getValue
        captured.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) mustBe Some(submittedDifferentDate)
        captured.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) must not be Some(currentDate)
      }
    }

    "must not change user answer when submitted value equals original start date" in {
      val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, editThirdPartyAccessStartDateRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> currentDate.getDayOfMonth.toString,
            "value.month" -> currentDate.getMonthValue.toString,
            "value.year"  -> currentDate.getYear.toString
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val captured = userAnswersCaptor.getValue
        captured.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) mustBe Some(currentDate)
      }
    }
  }
}
