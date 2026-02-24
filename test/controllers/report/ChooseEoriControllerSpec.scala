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
import controllers.actions.BelowReportRequestLimitAction
import controllers.problem.routes.TooManySubmissionsController
import forms.report.ChooseEoriFormProvider
import models.report.{ChooseEori, Decision, ReportDateRange, ReportTypeImport}
import models.requests.DataRequest
import models.{EoriRole, NormalMode, UserAnswers}
import navigation.{FakeReportNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.*
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.ChooseEoriView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class ChooseEoriControllerSpec extends SpecBase with MockitoSugar {

  val mockPassLimitAction: BelowReportRequestLimitAction & MockitoSugar = new BelowReportRequestLimitAction
    with MockitoSugar {
    override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] =
      Future.successful(
        Right(
          DataRequest(request.request, request.userId, request.eori, request.affinityGroup, request.userAnswers)
        )
      )

    override protected def executionContext: ExecutionContext = global
  }

  def onwardRoute: Call = Call("GET", "/request-customs-declaration-data/request-cds-report/eoriRole")

  lazy val chooseEoriRoute: String = controllers.report.routes.ChooseEoriController.onPageLoad(NormalMode).url

  val formProvider           = new ChooseEoriFormProvider()
  val form: Form[ChooseEori] = formProvider()

  "ChooseEori Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[BelowReportRequestLimitAction].toInstance(mockPassLimitAction)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, chooseEoriRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChooseEoriView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "GB123456789012")(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ChooseEoriPage, ChooseEori.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[BelowReportRequestLimitAction].toInstance(mockPassLimitAction)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, chooseEoriRoute)

        val view = application.injector.instanceOf[ChooseEoriView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ChooseEori.values.head), NormalMode, "GB123456789012")(
          request,
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
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", ChooseEori.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must populate ReportDateRange with custom date range when third party" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", ChooseEori.Myauthority.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportDateRangePage) mustBe Some(ReportDateRange.CustomDateRange)
      }
    }

    "must not populate ReportDateRange when user using own EORI" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", ChooseEori.Myeori.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportDateRangePage) mustBe None
      }
    }

    "if previous answer was Myeori and user selects my authority must clear any data and redirect to next page" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .success
        .value
        .set(SelectThirdPartyEoriPage, "eori")
        .success
        .value
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(EoriRolePage, Set(EoriRole.Declarant))
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .success
        .value
        .set(CustomRequestStartDatePage, LocalDate.now().minusDays(10))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.now().minusDays(5))
        .success
        .value
        .set(ReportNamePage, "name")
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", ChooseEori.Myauthority.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedUserAnswers = userAnswersCaptor.getValue
        capturedUserAnswers.get(CustomRequestStartDatePage) mustBe None
        capturedUserAnswers.get(SelectThirdPartyEoriPage) mustBe None
        capturedUserAnswers.get(DecisionPage) mustBe None
        capturedUserAnswers.get(EoriRolePage) mustBe None
        capturedUserAnswers.get(ReportTypeImportPage) mustBe None
        capturedUserAnswers.get(CustomRequestStartDatePage) mustBe None
        capturedUserAnswers.get(CustomRequestEndDatePage) mustBe None
        capturedUserAnswers.get(ReportNamePage) mustBe None
        capturedUserAnswers.get(ReportDateRangePage) mustBe Some(ReportDateRange.CustomDateRange)
      }
    }

    "if previous answer was Myeori and user selects Myeori again, must not clear any data and redirect to next page" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .success
        .value
        .set(CustomRequestStartDatePage, LocalDate.now().minusDays(10))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", ChooseEori.Myeori.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        val capturedUserAnswers = userAnswersCaptor.getValue
        capturedUserAnswers.get(CustomRequestStartDatePage) mustBe Some(LocalDate.now().minusDays(10))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ChooseEoriView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "GB123456789012")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, chooseEoriRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, chooseEoriRoute)
            .withFormUrlEncodedBody(("value", ChooseEori.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to TooManySubmissionsController for a GET when submission limit is reached" in {
      val mockAction = new BelowReportRequestLimitAction with MockitoSugar {
        override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] =
          Future.successful(Left(Redirect(TooManySubmissionsController.onPageLoad())))

        override protected def executionContext = global
      }

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[BelowReportRequestLimitAction].toInstance(mockAction))
        .build()

      running(application) {
        val request = FakeRequest(GET, chooseEoriRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TooManySubmissionsController.onPageLoad().url
      }
    }
  }
}
