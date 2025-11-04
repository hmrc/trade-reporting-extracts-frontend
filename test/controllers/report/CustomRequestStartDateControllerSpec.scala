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

package controllers.report

import base.SpecBase
import forms.report.CustomRequestStartDateFormProvider
import models.report.ReportTypeImport
import models.{NormalMode, ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.report.CustomRequestStartDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class CustomRequestStartDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]

  private val formProvider = new CustomRequestStartDateFormProvider()
  private val startDate    = LocalDate.now(ZoneOffset.UTC).minusYears(1)
  private def form         = formProvider(false, None, None)

  lazy val thirdPartyDetails = ThirdPartyDetails(
    None,
    LocalDate.of(2025, 1, 1),
    None,
    Set("imports"),
    None,
    None
  )

  def onwardRoute = Call("GET", "/foo")

  lazy val customRequestStartDateRoute: String =
    controllers.report.routes.CustomRequestStartDateController.onPageLoad(NormalMode).url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, customRequestStartDateRoute)

  def postRequest(date: LocalDate): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, customRequestStartDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )

  "CustomRequestStartDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false, false, None)(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when third party report with complete date range" in {

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetails))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SelectThirdPartyEoriPage, "traderEori").get))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false, true, None)(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when third party report with ongoing data range" in {

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(Future.successful(thirdPartyDetails.copy(dataStartDate = Some(LocalDate.of(2025, 1, 1)))))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SelectThirdPartyEoriPage, "traderEori").get))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          false,
          true,
          Some("You have access to data from 1 January 2025 onwards.")
        )(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when third party report with fixed data range" in {

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
              .copy(dataStartDate = Some(LocalDate.of(2025, 1, 1)), dataEndDate = Some(LocalDate.of(2025, 3, 1)))
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SelectThirdPartyEoriPage, "traderEori").get))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          false,
          true,
          Some("You have access to data from 1 January 2025 to 1 March 2025.")
        )(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must return correct view when more than one report selected" in {

      val ua = emptyUserAnswers
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportHeader, ReportTypeImport.ImportItem))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, true, false, None)(
          getRequest,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(CustomRequestStartDatePage, startDate).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[CustomRequestStartDateView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(startDate), NormalMode, false, false, None)(
          getRequest,
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
        val result = route(application, postRequest(startDate)).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "Value handling and cleanup" - {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      val prevStartDate         = LocalDate.now.minusMonths(2)
      val prevEndDate           = prevStartDate.plusDays(30)

      def createAppWithPrevDates: (Application, ArgumentCaptor[UserAnswers]) = {

        when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

        val userAnswers = emptyUserAnswers
          .set(CustomRequestStartDatePage, prevStartDate)
          .success
          .value
          .set(CustomRequestEndDatePage, prevEndDate)
          .success
          .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        (application, userAnswersCaptor)
      }

      "must cleanup end date when new start date before old start date" in {

        val (application, userAnswersCaptor) = createAppWithPrevDates

        running(application) {
          val result = route(application, postRequest(prevStartDate.minusDays(1))).value

          status(result) mustEqual SEE_OTHER
          val capturedAnswers = userAnswersCaptor.getValue
          capturedAnswers.get(CustomRequestStartDatePage) mustBe Some(prevStartDate.minusDays(1))
          capturedAnswers.get(CustomRequestEndDatePage) mustBe None
        }
      }

      "must not cleanup end date when new start date == old start date" in {

        val (application, userAnswersCaptor) = createAppWithPrevDates

        running(application) {
          val result = route(application, postRequest(prevStartDate)).value

          status(result) mustEqual SEE_OTHER
          val capturedAnswers = userAnswersCaptor.getValue
          capturedAnswers.get(CustomRequestStartDatePage) mustBe Some(prevStartDate)
          capturedAnswers.get(CustomRequestEndDatePage) mustBe Some(prevEndDate)
        }
      }

      "must not cleanup end date when new start date is between old start date and end date" in {

        val (application, userAnswersCaptor) = createAppWithPrevDates

        running(application) {
          val result = route(application, postRequest(prevStartDate.plusWeeks(1))).value

          status(result) mustEqual SEE_OTHER
          val capturedAnswers = userAnswersCaptor.getValue
          capturedAnswers.get(CustomRequestStartDatePage) mustBe Some(prevStartDate.plusWeeks(1))
          capturedAnswers.get(CustomRequestEndDatePage) mustBe Some(prevEndDate)
        }
      }

      "must not cleanup end date when new start date == end date" in {

        val (application, userAnswersCaptor) = createAppWithPrevDates

        running(application) {
          val result = route(application, postRequest(prevEndDate)).value

          status(result) mustEqual SEE_OTHER
          val capturedAnswers = userAnswersCaptor.getValue
          capturedAnswers.get(CustomRequestStartDatePage) mustBe Some(prevEndDate)
          capturedAnswers.get(CustomRequestEndDatePage) mustBe Some(prevEndDate)
        }
      }

      "must cleanup end date when new start date is after end date" in {

        val (application, userAnswersCaptor) = createAppWithPrevDates

        running(application) {
          val result = route(application, postRequest(prevEndDate.plusWeeks(1))).value

          status(result) mustEqual SEE_OTHER
          val capturedAnswers = userAnswersCaptor.getValue
          capturedAnswers.get(CustomRequestStartDatePage) mustBe Some(prevEndDate.plusWeeks(1))
          capturedAnswers.get(CustomRequestEndDatePage) mustBe None
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted in a third party scenario" in {

      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any()))
        .thenReturn(
          Future.successful(
            thirdPartyDetails
              .copy(dataStartDate = Some(LocalDate.of(2025, 1, 1)), dataEndDate = Some(LocalDate.of(2025, 3, 1)))
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SelectThirdPartyEoriPage, "traderEori").get))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      val request =
        FakeRequest(POST, customRequestStartDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          false,
          true,
          Some("You have access to data from 1 January 2025 to 1 March 2025.")
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, customRequestStartDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CustomRequestStartDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, false, false, None)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest(startDate)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
