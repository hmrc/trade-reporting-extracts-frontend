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
import forms.editThirdParty.EditThirdPartyAccessEndDateFormProvider
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import pages.editThirdParty.{EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.editThirdParty.EditThirdPartyAccessEndDateView

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future

class EditThirdPartyAccessEndDateControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {
  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  private val mockSessionRepository             = mock[SessionRepository]
  override def fakeApplication()                =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(
        bind[Clock].toInstance(fixedClock),
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
      )
      .build()
  override protected def beforeEach(): Unit     = {
    super.beforeEach()
    org.mockito.Mockito.reset(mockTradeReportingExtractsService, mockSessionRepository)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  private implicit val messages: Messages = stubMessages()

  private val formProvider               = new EditThirdPartyAccessEndDateFormProvider()
  private def form(startDate: LocalDate) = formProvider(startDate)(messages)
  def onwardRoute: Call                  = Call("GET", "/request-customs-declaration-data/third-party-details-GB123456789000")

  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)
  val thirdPartyEori         = "GB123456789000"

  lazy val editThirdPartyAccessEndDateRoute =
    controllers.editThirdParty.routes.EditThirdPartyAccessEndDateController.onPageLoad(thirdPartyEori).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, editThirdPartyAccessEndDateRoute)

  def postNoEndDateRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, editThirdPartyAccessEndDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> "",
        "value.month" -> "",
        "value.year"  -> ""
      )

  def postRequest(date: LocalDate): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, editThirdPartyAccessEndDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )

  val instant: Instant      = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
  val fixedClock: Clock     = Clock.fixed(instant, ZoneOffset.UTC)
  val fixedToday: LocalDate = LocalDate.now(ZoneOffset.UTC)

  private def expectedHint(startDate: LocalDate): String = {
    val base = if (startDate.isBefore(fixedToday)) fixedToday else startDate
    base.plusMonths(1).format(utils.DateTimeFormats.dateTimeHintFormat)
  }

  "EditThirdPartyAccessEndDateController" - {

    "must return OK and the correct view for a GET" in {
      val pageStartDate = LocalDate.of(2024, 1, 1)
      val userAnswers   =
        emptyUserAnswers.set(EditThirdPartyAccessStartDatePage(thirdPartyEori), pageStartDate).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val result = route(application, getRequest()).value
      val view   = application.injector.instanceOf[EditThirdPartyAccessEndDateView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        form(pageStartDate),
        utils.DateTimeFormats.dateFormatter(pageStartDate),
        thirdPartyEori,
        expectedHint(pageStartDate)
      )(getRequest(), messages(application)).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val pageStartDate = LocalDate.of(2024, 1, 1)
      val userAnswers   = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), pageStartDate)
        .success
        .value
        .set(EditThirdPartyAccessEndDatePage(thirdPartyEori), validAnswer)
        .success
        .value

      setUserAnswers(Some(userAnswers))
      val application = app

      val view   = application.injector.instanceOf[EditThirdPartyAccessEndDateView]
      val result = route(application, getRequest()).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        form(pageStartDate).fill(Some(validAnswer)),
        utils.DateTimeFormats.dateFormatter(pageStartDate),
        thirdPartyEori,
        expectedHint(pageStartDate)
      )(getRequest(), messages(application)).toString
    }

    "must redirect to edit third party general problem page if EditThirdPartyAccessStartDatePage is missing on GET" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val result = route(application, getRequest()).value

      status(result) mustEqual SEE_OTHER
      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) mustBe None
      redirectLocation(result).value mustEqual controllers.problem.routes.EditThirdPartyGeneralProblemController
        .onPageLoad()
        .url
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any())) thenReturn Future.successful(
        ThirdPartyDetails(
          referenceName = Some(thirdPartyEori),
          accessStartDate = LocalDate.of(2024, 1, 1),
          accessEndDate = None,
          dataTypes = Set("IMPORTS"),
          dataStartDate = None,
          dataEndDate = None
        )
      )

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), LocalDate.of(2024, 1, 1))
        .success
        .value
      setUserAnswers(Some(userAnswers))
      val application = app
      val validDate   = LocalDate.now().plusDays(1)

      val result = route(application, postRequest(validDate)).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must redirect to edit third party general problem page if EditThirdPartyAccessStartDatePage is missing on POST" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val result = route(application, postRequest(LocalDate.now().plusDays(1))).value

      status(result) mustEqual SEE_OTHER
      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) mustBe None
      redirectLocation(result).value mustEqual controllers.problem.routes.EditThirdPartyGeneralProblemController
        .onPageLoad()
        .url
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      setUserAnswers(None)
      val application = app

      val result = route(application, getRequest()).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      setUserAnswers(None)
      val application       = app
      val originalStartDate = fixedToday
      val invalidDate       = originalStartDate.minusDays(1)

      val result = route(application, postRequest(invalidDate)).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must set a user answer when submitted value differs from original end date" in {
      val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])

      val originalStartDate = LocalDate.of(2024, 1, 1)
      val originalEndDate   = LocalDate.now().plusDays(1)
      val changedEndDate    = LocalDate.now().plusDays(2)

      val thirdPartyDetailsResponse =
        ThirdPartyDetails(
          referenceName = Some(thirdPartyEori),
          accessStartDate = originalStartDate,
          accessEndDate = Some(originalEndDate),
          dataTypes = Set.empty,
          dataStartDate = None,
          dataEndDate = None
        )

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), originalStartDate)
        .success
        .value
      setUserAnswers(Some(userAnswers))
      val application = app

      val result = route(application, postRequest(changedEndDate)).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      val updated = captor.getValue
      updated.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) mustBe Some(changedEndDate)
      updated.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) mustBe Some(originalStartDate) // retained
    }

    "must remove start date and not set end date when submitted value equals original end date and start date unchanged" in {
      val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])

      val originalStartDate = LocalDate.of(2024, 1, 1)
      val originalEndDate   = LocalDate.now().plusDays(1)

      val thirdPartyDetailsResponse =
        ThirdPartyDetails(
          referenceName = Some(thirdPartyEori),
          accessStartDate = originalStartDate,
          accessEndDate = Some(originalEndDate),
          dataTypes = Set.empty,
          dataStartDate = None,
          dataEndDate = None
        )

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), originalStartDate)
        .success
        .value
      setUserAnswers(Some(userAnswers))
      val application = app

      val result = route(application, postRequest(originalEndDate)).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      val updated = captor.getValue
      updated.get(EditThirdPartyAccessStartDatePage(thirdPartyEori)) mustBe None
      updated.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) mustBe None
    }

    "must persist LocalDate.MAX when user selects 'No End Date' but there was an original end date" in {

      val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])

      val originalStartDate = LocalDate.of(2024, 1, 1)
      val originalEndDate   = LocalDate.now().plusDays(1)

      val thirdPartyDetailsResponse =
        ThirdPartyDetails(
          referenceName = Some(thirdPartyEori),
          accessStartDate = originalStartDate,
          accessEndDate = Some(originalEndDate),
          dataTypes = Set.empty,
          dataStartDate = None,
          dataEndDate = None
        )

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), originalStartDate)
        .success
        .value
      setUserAnswers(Some(userAnswers))
      val application = app

      val result = route(application, postNoEndDateRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      val updated = captor.getValue
      updated.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) mustBe Some(LocalDate.MAX)
    }

    "must not persist end date when user selects 'No End Date' and there was no original end date" in {

      val captor = org.mockito.ArgumentCaptor.forClass(classOf[UserAnswers])

      val originalStartDate = LocalDate.of(2024, 1, 1)

      val thirdPartyDetailsResponse =
        ThirdPartyDetails(
          referenceName = Some(thirdPartyEori),
          accessStartDate = originalStartDate,
          accessEndDate = None,
          dataTypes = Set.empty,
          dataStartDate = None,
          dataEndDate = None
        )

      when(mockTradeReportingExtractsService.getThirdPartyDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetailsResponse))
      when(mockSessionRepository.set(captor.capture())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(EditThirdPartyAccessStartDatePage(thirdPartyEori), originalStartDate)
        .success
        .value
      setUserAnswers(Some(userAnswers))
      val application = app

      val result = route(application, postNoEndDateRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      val updated = captor.getValue
      updated.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)) mustBe None
    }

  }
}
