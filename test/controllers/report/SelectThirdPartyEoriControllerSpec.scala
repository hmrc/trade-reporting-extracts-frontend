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
import forms.report.SelectThirdPartyEoriFormProvider
import models.report.{ChooseEori, Decision, ReportTypeImport}
import models.{NormalMode, SectionNavigation, SelectThirdPartyEori, UserAnswers}
import navigation.{FakeNavigator, FakeReportNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.{ChooseEoriPage, DecisionPage, ReportNamePage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.problem.NoThirdPartyAccessView
import views.html.report.SelectThirdPartyEoriView

import scala.concurrent.Future

class SelectThirdPartyEoriControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/request-customs-declaration-data/data-download")

  lazy val selectThirdPartyEoriRoute =
    controllers.report.routes.SelectThirdPartyEoriController.onPageLoad(NormalMode).url

  val formProvider = new SelectThirdPartyEoriFormProvider()
  val form         = formProvider()

  val eoriList: SelectThirdPartyEori = SelectThirdPartyEori(
    Seq("business1 testEori1", "business2 testEori2", "business3 testEori3")
  )

  "SelectThirdPartyEori Controller" - {

    "must return OK and the correct view for a GET and render radios" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
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
    }

    "must redirect to NoThirdPartyAccess and remove section navigation and answers when getSelectThirdPartyEori returns empty" in {
      val emptyEoriList: SelectThirdPartyEori = SelectThirdPartyEori(
        Seq()
      )
      val sectionNav                          = SectionNavigation("reportRequestSection")

      def noThirdPartyAccessRoute: Call = Call("GET", "/request-customs-declaration-data/no-third-party-access")

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
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

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(GET, selectThirdPartyEoriRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoThirdPartyAccessView]

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual noThirdPartyAccessRoute.url

        val capturedAnswers = userAnswersCaptor.getValue

        capturedAnswers.get(ReportTypeImportPage) mustBe None
        capturedAnswers.get(sectionNav) mustBe None
        capturedAnswers.get(DecisionPage) mustBe None
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(SelectThirdPartyEoriPage, "answer").success.value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, selectThirdPartyEoriRoute)

        val view = application.injector.instanceOf[SelectThirdPartyEoriView]

        val result = route(application, request).value

        status(result) `mustEqual` OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, eoriList)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page and not set a user answer to ReportTypeImportPage when decision is import" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )

      val userAnswers = UserAnswers(userAnswersId)
        .set(DecisionPage, Decision.Import)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, selectThirdPartyEoriRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportTypeImportPage) mustBe None
      }
    }

    "must redirect to the next page and set a user answer export to ReportTypeImportPage when decision is export" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getSelectThirdPartyEori(any())(any())) thenReturn Future.successful(
        eoriList
      )
      val userAnswers                       = UserAnswers(userAnswersId)
        .set(DecisionPage, Decision.Export)
        .success
        .value
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, selectThirdPartyEoriRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportTypeImportPage) mustBe Some(Set(ReportTypeImport.ExportItem))
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, selectThirdPartyEoriRoute)

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, selectThirdPartyEoriRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
