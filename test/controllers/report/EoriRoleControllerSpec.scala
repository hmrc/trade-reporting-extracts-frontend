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
import forms.report.EoriRoleFormProvider
import models.report.{Decision, ReportTypeImport}
import models.{EoriRole, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.{DecisionPage, EoriRolePage, ReportTypeImportPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.report.EoriRoleView

import scala.concurrent.Future

class EoriRoleControllerSpec extends SpecBase with MockitoSugar {

  def onwardRouteImport: Call = Call("GET", "/request-customs-declaration-data/import-report-type")
  def onwardRouteExport: Call = Call("GET", "/request-customs-declaration-data/date-rage")

  lazy val eoriRoleRoute: String = controllers.report.routes.EoriRoleController.onPageLoad(NormalMode).url

  val formProvider              = new EoriRoleFormProvider()
  val form: Form[Set[EoriRole]] = formProvider()

  val userAnswersImporter: UserAnswers = emptyUserAnswers
    .set(
      DecisionPage,
      Decision.Import
    )
    .success
    .value
  val userAnswersExporter: UserAnswers = emptyUserAnswers
    .set(
      DecisionPage,
      Decision.Export
    )
    .success
    .value

  "EoriRole Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersImporter)).build()

      running(application) {
        val request = FakeRequest(GET, eoriRoleRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EoriRoleView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, true)(request, messages(application)).toString
      }
    }

    "must populate the view correctly when Importer selected" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersImporter)).build()

      running(application) {
        val request = FakeRequest(GET, eoriRoleRoute)

        val view = application.injector.instanceOf[EoriRoleView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, true)(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("Importer") mustBe true
        contentAsString(result).contains("Exporter") mustBe false
        contentAsString(result).contains("Declarant") mustBe true

      }

    }

    "must populate the view correctly when Exporter selected" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersExporter)).build()

      running(application) {
        val request = FakeRequest(GET, eoriRoleRoute)

        val view = application.injector.instanceOf[EoriRoleView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false)(
          request,
          messages(application)
        ).toString
        contentAsString(result).contains("Importer") mustBe false
        contentAsString(result).contains("Exporter") mustBe true
        contentAsString(result).contains("Declarant") mustBe true

      }

    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersImporter.set(EoriRolePage, Set(EoriRole.Importer, EoriRole.Declarant)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, eoriRoleRoute)

        val view = application.injector.instanceOf[EoriRoleView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Set(EoriRole.Importer, EoriRole.Declarant)), NormalMode, true)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page and not set a user answer to ReportTypeImportPage when decision is import" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = userAnswersImporter
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteImport)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, eoriRoleRoute)
            .withFormUrlEncodedBody(("value[0]", EoriRole.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteImport.url

        val capturedAnswers = userAnswersCaptor.getValue
        capturedAnswers.get(ReportTypeImportPage) mustBe None
      }
    }

    "must redirect to the next page and set a user answer export to ReportTypeImportPage when decision is export" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswersCaptor     = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(userAnswersCaptor.capture())) thenReturn Future.successful(true)

      val userAnswers = userAnswersExporter
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteExport)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, eoriRoleRoute)
            .withFormUrlEncodedBody(("value[0]", EoriRole.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteExport.url

        val capturedAnswers = userAnswersCaptor.getValue

        capturedAnswers.get(ReportTypeImportPage) mustBe Some(Set(ReportTypeImport.ExportItem))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersImporter)).build()

      running(application) {
        val request =
          FakeRequest(POST, eoriRoleRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EoriRoleView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, true)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, eoriRoleRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, eoriRoleRoute)
            .withFormUrlEncodedBody(("value[0]", EoriRole.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
