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
import controllers.routes
import forms.report.CustomRequestEndDateFormProvider
import models.report.ReportTypeImport
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, ReportTypeImportPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.CustomRequestEndDateView

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class CustomRequestEndDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider                      = new CustomRequestEndDateFormProvider()
  private val mostRecentPossibleStartDate       = LocalDate.now(ZoneOffset.UTC).minusDays(3)
  private val mostRecentPossibleStartDateString =
    mostRecentPossibleStartDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
  private val startDate                         = LocalDate.now(ZoneOffset.UTC).minusYears(1)
  private val startDateString                   = startDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
  private val startDatePlus31DaysString         = startDate.plusDays(31).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

  private def form = formProvider(mostRecentPossibleStartDate)

  def onwardRoute = Call("GET", "/foo")

  lazy val customRequestEndDateRoute =
    controllers.report.routes.CustomRequestEndDateController.onPageLoad(NormalMode).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, customRequestEndDateRoute)

  def postRequest(date: LocalDate): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, customRequestEndDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )

  "CustomRequestEndDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswers.set(CustomRequestStartDatePage, startDate).success.value)
      ).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[CustomRequestEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, startDateString, startDatePlus31DaysString, false)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must return correct view when more than one report type selected " in {

      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .set(CustomRequestStartDatePage, startDate)
            .success
            .value
            .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem, ReportTypeImport.ImportHeader))
            .success
            .value
        )
      ).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[CustomRequestEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, startDateString, startDatePlus31DaysString, true)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must return maximum date as today minus 2 days when start date +31days is in the future" in {

      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .set(CustomRequestStartDatePage, mostRecentPossibleStartDate)
            .success
            .value
            .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
            .success
            .value
        )
      ).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[CustomRequestEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          mostRecentPossibleStartDateString,
          mostRecentPossibleStartDateString,
          false
        )(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(CustomRequestStartDatePage, startDate)
        .success
        .value
        .set(CustomRequestEndDatePage, startDate)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[CustomRequestEndDateView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(startDate),
          NormalMode,
          startDateString,
          startDatePlus31DaysString,
          false
        )(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val ua                    = emptyUserAnswers
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem, ReportTypeImport.ImportHeader))
        .success
        .value
        .set(CustomRequestStartDatePage, startDate)
        .success
        .value
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(ua))
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

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ua = emptyUserAnswers
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .success
        .value
        .set(CustomRequestStartDatePage, startDate)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request =
        FakeRequest(POST, customRequestEndDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CustomRequestEndDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          startDateString,
          startDatePlus31DaysString,
          false
        )(request, messages(application)).toString
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
        val result = route(application, postRequest(startDate)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
