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
import forms.report.AccountsYouHaveAuthorityOverImportFormProvider
import models.report.Decision
import models.{NormalMode, ReportTypeImport, UserAnswers}
import navigation.{FakeReportNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.{AccountsYouHaveAuthorityOverImportPage, DecisionPage, ReportTypeImportPage}
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import views.html.report.AccountsYouHaveAuthorityOverImportView

import scala.concurrent.Future

class AccountsYouHaveAuthorityOverImportControllerSpec extends SpecBase with MockitoSugar {
  private implicit val messages: Messages = stubMessages()

  def onwardRouteImport: Call = Call("GET", "/request-customs-declaration-data/report-type")
  def onwardRouteExport: Call = Call("GET", "/request-customs-declaration-data/date-rage")

  val formProvider = new AccountsYouHaveAuthorityOverImportFormProvider()
  val form         = formProvider()

  val eoriList = Seq(
    SelectItem(text = messages("accountsYouHaveAuthorityOverImport.defaultValue")),
    SelectItem(text = "test1")
  ): Seq[SelectItem]

  lazy val accountsYouHaveAuthorityOverImportRoute =
    controllers.report.routes.AccountsYouHaveAuthorityOverImportController.onPageLoad(NormalMode).url

  "AccountsYouHaveAuthorityOverImport Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getEoriList()(any[Messages])) thenReturn Future.successful(eoriList)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, accountsYouHaveAuthorityOverImportRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountsYouHaveAuthorityOverImportView]

        status(result) `mustEqual` OK
        contentAsString(result) mustEqual view(form, NormalMode, eoriList)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(AccountsYouHaveAuthorityOverImportPage, "answer").success.value

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getEoriList()(any[Messages])) thenReturn Future.successful(eoriList)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, accountsYouHaveAuthorityOverImportRoute)

        val view = application.injector.instanceOf[AccountsYouHaveAuthorityOverImportView]

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
      when(mockTradeReportingExtractsService.getEoriList()(any[Messages])) thenReturn Future.successful(eoriList)

      val userAnswers = UserAnswers(userAnswersId)
        .set(DecisionPage, Decision.Import)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRouteImport)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountsYouHaveAuthorityOverImportRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteImport.url

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportTypeImportPage) mustBe None
      }
    }

    "must redirect to the next page and set a user answer export to ReportTypeImportPage when decision is export" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getEoriList()(any[Messages])) thenReturn Future.successful(eoriList)

      val userAnswers = UserAnswers(userAnswersId)
        .set(DecisionPage, Decision.Export)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeReportNavigator(onwardRouteExport)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, accountsYouHaveAuthorityOverImportRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteExport.url

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportTypeImportPage) mustBe Some(Set(ReportTypeImport.ExportItem))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getEoriList()(any[Messages])) thenReturn Future.successful(eoriList)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, accountsYouHaveAuthorityOverImportRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AccountsYouHaveAuthorityOverImportView]

        val result = route(application, request).value

        status(result) `mustEqual` BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, eoriList)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, accountsYouHaveAuthorityOverImportRoute)

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, accountsYouHaveAuthorityOverImportRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
