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
import exceptions.NoAuthorisedUserFoundException
import forms.report.SelectThirdPartyEoriFormProvider
import models.report.{Decision, ReportTypeImport}
import models.{AlreadySubmittedFlag, NormalMode, SectionNavigation, SelectThirdPartyEori, UserAnswers}
import navigation.{FakeReportNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import pages.report.{DecisionPage, ReportNamePage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.report.SelectThirdPartyEoriView

import java.time.LocalDate
import scala.concurrent.Future

class SelectThirdPartyEoriControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSessionRepository             = mock[SessionRepository]
  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  private var navigatorRoute: Call              = Call("GET", "/")
  private val mockNavigator                     = new FakeReportNavigator(Call("GET", "/")) {
    override def nextPage(page: pages.Page, mode: models.Mode, userAnswers: UserAnswers): Call = navigatorRoute
  }

  def onwardRoute: Call       = Call("GET", "/request-customs-declaration-data/data-download")
  def onwardRouteImport: Call = Call("GET", "/request-customs-declaration-data/import-report-type")
  def onwardRouteExport: Call = Call("GET", "/request-customs-declaration-data/export-item-report")

  lazy val selectThirdPartyEoriRoute =
    controllers.report.routes.SelectThirdPartyEoriController.onPageLoad(NormalMode).url

  val formProvider = new SelectThirdPartyEoriFormProvider()
  val form         = formProvider()

  val eoriList: SelectThirdPartyEori = SelectThirdPartyEori(
    Seq("business1 testEori1", "business2 testEori2", "business3 testEori3"),
    Seq("testEori1", "testEori2", "testEori3")
  )

  val dummyDate = LocalDate.of(2024, 1, 1)

  override def fakeApplication() =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(
        bind[Navigator].toInstance(mockNavigator),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
      )
      .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockTradeReportingExtractsService)
    navigatorRoute = onwardRoute
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
  }

  "SelectThirdPartyEori Controller" - {

    "must return OK and the correct view for a GET and render radios" in {

      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request = FakeRequest(GET, selectThirdPartyEoriRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[SelectThirdPartyEoriView]

      val body = contentAsString(result)

      body mustEqual view(form, NormalMode, eoriList)(request, messages(application)).toString

      body must include("business1 testEori1")
      body must include("business2 testEori2")
      body must include("business3 testEori3")
      body must not include "NonexistingBusiness"
    }

    "must redirect to NoThirdPartyAccess and remove section navigation and answers when getSelectThirdPartyEori returns empty" in {
      val emptyEoriList: SelectThirdPartyEori = SelectThirdPartyEori(
        Seq(),
        Seq()
      )
      val sectionNav                          = SectionNavigation("reportRequestSection")

      def noThirdPartyAccessRoute: Call = Call("GET", "/request-customs-declaration-data/no-third-party-access")

      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        emptyEoriList
      )

      val userAnswers = UserAnswers(userAnswersId)
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(sectionNav, "/foo")
        .success
        .value
        .set(ReportNamePage, "foo")
        .success
        .value

      navigatorRoute = onwardRoute
      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(GET, selectThirdPartyEoriRoute)

      val result = route(application, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value mustEqual noThirdPartyAccessRoute.url

      val capturedAnswers = userAnswersCaptor.getValue

      capturedAnswers.get(ReportTypeImportPage) mustBe None
      capturedAnswers.get(sectionNav) mustBe None
      capturedAnswers.get(DecisionPage) mustBe None
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(SelectThirdPartyEoriPage, "answer").success.value

      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )

      setUserAnswers(Some(userAnswers))
      val application = app

      val request = FakeRequest(GET, selectThirdPartyEoriRoute)

      val view = application.injector.instanceOf[SelectThirdPartyEoriView]

      val result = route(application, request).value

      status(result) `mustEqual` OK
      contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, eoriList)(
        request,
        messages(application)
      ).toString
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request = FakeRequest(GET, selectThirdPartyEoriRoute)

      val result = route(application, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      setUserAnswers(None)
      val application = app

      val request =
        FakeRequest(POST, selectThirdPartyEoriRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) `mustEqual` SEE_OTHER
      redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
    }

    "must update answers and set decision/export when dataTypes is exports" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val thirdPartyDetailsExports = models.ThirdPartyDetails(
        referenceName = Some("answer"),
        accessStartDate = dummyDate,
        accessEndDate = None,
        dataTypes = Set("exports"),
        dataStartDate = None,
        dataEndDate = None
      )
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )
      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any())) thenReturn Future
        .successful(thirdPartyDetailsExports)

      val userAnswers = UserAnswers(userAnswersId)

      navigatorRoute = onwardRouteExport
      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, selectThirdPartyEoriRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRouteExport.url

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(SelectThirdPartyEoriPage) mustBe Some("answer")
      capturedAnswers.get(DecisionPage) mustBe Some(Decision.Export)
      capturedAnswers.get(ReportTypeImportPage) mustBe Some(Set(ReportTypeImport.ExportItem))
    }

    "must update answers and set decision/import when dataTypes is imports" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val thirdPartyDetailsImports = models.ThirdPartyDetails(
        referenceName = Some("answer"),
        accessStartDate = dummyDate,
        accessEndDate = None,
        dataTypes = Set("imports"),
        dataStartDate = None,
        dataEndDate = None
      )
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )
      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any())) thenReturn Future
        .successful(thirdPartyDetailsImports)

      val userAnswers = UserAnswers(userAnswersId)

      navigatorRoute = onwardRouteImport
      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, selectThirdPartyEoriRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRouteImport.url

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(SelectThirdPartyEoriPage) mustBe Some("answer")
      capturedAnswers.get(DecisionPage) mustBe Some(Decision.Import)
      capturedAnswers.get(ReportTypeImportPage) mustBe None
    }

    "must remove decision when dataTypes is both imports and exports" in {
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val thirdPartyDetailsOther = models.ThirdPartyDetails(
        referenceName = Some("answer"),
        accessStartDate = dummyDate,
        accessEndDate = None,
        dataTypes = Set("imports", "exports"),
        dataStartDate = None,
        dataEndDate = None
      )
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )
      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any())) thenReturn Future
        .successful(thirdPartyDetailsOther)

      val userAnswers = UserAnswers(userAnswersId).set(DecisionPage, Decision.Import).success.value

      navigatorRoute = onwardRoute
      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, selectThirdPartyEoriRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(SelectThirdPartyEoriPage) mustBe Some("answer")
      capturedAnswers.get(DecisionPage) mustBe None
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )

      setUserAnswers(Some(emptyUserAnswers))
      val application = app

      val request =
        FakeRequest(POST, selectThirdPartyEoriRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      val view      = application.injector.instanceOf[SelectThirdPartyEoriView]
      val boundForm = form.bind(Map("value" -> ""))

      contentAsString(result) mustEqual view(boundForm, NormalMode, eoriList)(
        request,
        messages(application)
      ).toString
    }

    "must redirect to RequestNotCompletedController when NoAuthorisedUserFoundException is thrown" in {

      val testEori          = "answer"
      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )
      when(mockTradeReportingExtractsService.getAuthorisedBusinessDetails(any(), any())(any())) thenReturn Future
        .failed(new NoAuthorisedUserFoundException("Test exception"))

      val sectionNav  = SectionNavigation("reportRequestSection")
      val userAnswers = UserAnswers(userAnswersId)
        .set(DecisionPage, Decision.Import)
        .success
        .value
        .set(sectionNav, "/foo")
        .success
        .value
        .set(ReportNamePage, "foo")
        .success
        .value

      setUserAnswers(Some(userAnswers))
      val application = app

      val request =
        FakeRequest(POST, selectThirdPartyEoriRoute)
          .withFormUrlEncodedBody(("value", testEori))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.report.routes.RequestNotCompletedController
        .onPageLoad(testEori)
        .url

      val capturedAnswers = userAnswersCaptor.getValue
      capturedAnswers.get(ReportTypeImportPage) mustBe None
      capturedAnswers.get(sectionNav) mustBe None
      capturedAnswers.get(DecisionPage) mustBe None
      capturedAnswers.get(ReportNamePage) mustBe None
      capturedAnswers.get(AlreadySubmittedFlag()) mustBe Some(true)
    }

  }
}
