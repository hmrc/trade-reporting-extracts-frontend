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

import base.AppPerSuiteSpecBase
import controllers.actions.BelowReportRequestLimitAction
import controllers.problem.routes
import forms.report.DecisionFormProvider
import models.report.{ChooseEori, Decision, ReportTypeImport}
import models.requests.DataRequest
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import pages.report.{ChooseEoriPage, DecisionPage, EoriRolePage, ReportTypeImportPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.DecisionView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DecisionControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {

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

  private var navigatorRoute: Call  = Call("GET", "/")
  private val mockNavigator         = new Navigator {
    override def nextPage(page: pages.Page, mode: models.Mode, userAnswers: UserAnswers): Call = navigatorRoute
    override val normalRoutes: pages.Page => UserAnswers => Call                               = _ => _ => navigatorRoute
    override val checkRoutes: pages.Page => UserAnswers => Call                                = _ => _ => navigatorRoute
    override val normalRoutesWithFlag: pages.Page => UserAnswers => Boolean => Call            = _ => _ => _ => navigatorRoute
    override val checkRoutesWithFlag: pages.Page => UserAnswers => Boolean => Call             = _ => _ => _ => navigatorRoute
  }
  private val mockSessionRepository = mock[SessionRepository]

  def onwardRoute                 = Call("GET", "/request-customs-declaration-data/request-data")
  def onwardRouteThirdPartyImport = Call("GET", "/request-customs-declaration-data/import-report-type")
  def onwardRouteThirdPartyExport = Call("GET", "/request-customs-declaration-data/export-item-report")

  lazy val decisionRoute: String = controllers.report.routes.DecisionController.onPageLoad(NormalMode).url

  val formProvider         = new DecisionFormProvider()
  val form: Form[Decision] = formProvider()

  override def fakeApplication() =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(
        bind[BelowReportRequestLimitAction].toInstance(mockPassLimitAction),
        bind[Navigator].toInstance(mockNavigator),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    navigatorRoute = onwardRoute
    reset(mockSessionRepository)
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
  }

  "Decision Controller" - {

    "must return OK and the correct view for a GET" in {

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request = FakeRequest(GET, decisionRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[DecisionView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(DecisionPage, Decision.values.head).success.value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, decisionRoute)

      val view = application.injector.instanceOf[DecisionView]

      val result = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form.fill(Decision.values.head), NormalMode)(
        request,
        messages(application)
      ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      navigatorRoute = onwardRoute
      val ua = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .success
        .value

      setUserAnswers(Some(ua))
      val application = app

      val request =
        FakeRequest(POST, decisionRoute)
          .withFormUrlEncodedBody(("value", Decision.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must redirect to ReportTypeImportPage when ChooseEori is Myauthority and Decision is Import" in {

      navigatorRoute = onwardRouteThirdPartyImport
      val ua = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value

      setUserAnswers(Some(ua))
      val application = app

      val request =
        FakeRequest(POST, decisionRoute)
          .withFormUrlEncodedBody(("value", Decision.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRouteThirdPartyImport.url
    }

    "must redirect to export-item-report and set a user answer export to ReportTypeImportPage when thirdParty and Decision is Export" in {
      navigatorRoute = onwardRouteThirdPartyExport
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val ua = emptyUserAnswers
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value

      setUserAnswers(Some(ua))
      val application = app

      val request = FakeRequest(POST, decisionRoute)
        .withFormUrlEncodedBody("value" -> Decision.Export.toString)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRouteThirdPartyExport.url

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(DecisionPage) mustBe Some(Decision.Export)
      capturedAnswers.get(ReportTypeImportPage) mustBe Some(Set(ReportTypeImport.ExportItem))
      capturedAnswers.get(EoriRolePage) mustBe None
    }

    "must cleanup correctly when change of answers from export to import" in {
      navigatorRoute = onwardRouteThirdPartyExport
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())).thenReturn(Future.successful(true))

      val ua = emptyUserAnswers
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value
        .set(DecisionPage, Decision.Export)
        .success
        .value

      setUserAnswers(Some(ua))
      val application = app

      val request = FakeRequest(POST, decisionRoute)
        .withFormUrlEncodedBody("value" -> Decision.Import.toString)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(DecisionPage) mustBe Some(Decision.Import)
      capturedAnswers.get(ReportTypeImportPage) mustBe None
      capturedAnswers.get(EoriRolePage) mustBe None
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, decisionRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[DecisionView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request = FakeRequest(GET, decisionRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request =
        FakeRequest(POST, decisionRoute)
          .withFormUrlEncodedBody(("value", Decision.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

  }
}
