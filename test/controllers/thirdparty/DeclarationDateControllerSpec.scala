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

package controllers.thirdparty

import base.SpecBase
import controllers.routes
import forms.thirdparty.DeclarationDateFormProvider
import models.thirdparty.{DataTypes, DeclarationDate}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.thirdparty.{DataTypesPage, DeclarationDatePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.thirdparty.DeclarationDateView

import scala.concurrent.Future

class DeclarationDateControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val declarationDateRoute = controllers.thirdparty.routes.DeclarationDateController.onPageLoad(NormalMode).url

  val formProvider = new DeclarationDateFormProvider()

  "DeclarationDate Controller" - {

    val importOnlyAnswers = emptyUserAnswers.set(DataTypesPage, Set(DataTypes.Import)).success.value
    val exportOnlyAnswers = emptyUserAnswers.set(DataTypesPage, Set(DataTypes.Export)).success.value
    val bothAnswers       = emptyUserAnswers.set(DataTypesPage, Set(DataTypes.Import, DataTypes.Export)).success.value
    val noAnswers         = emptyUserAnswers

    "must return OK and the correct view for a GET with import only" in {
      val application = applicationBuilder(userAnswers = Some(importOnlyAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, declarationDateRoute)
        val view    = application.injector.instanceOf[DeclarationDateView]
        val form    = formProvider(Seq("import"))
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "import")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with export only" in {
      val application = applicationBuilder(userAnswers = Some(exportOnlyAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, declarationDateRoute)
        val view    = application.injector.instanceOf[DeclarationDateView]
        val form    = formProvider(Seq("export"))
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "export")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with import and export" in {
      val application = applicationBuilder(userAnswers = Some(bothAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, declarationDateRoute)
        val view    = application.injector.instanceOf[DeclarationDateView]
        val form    = formProvider(Seq("import and export"))
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "import and export")(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET with no DataTypes" in {
      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, declarationDateRoute)
        val view    = application.injector.instanceOf[DeclarationDateView]
        val form    = formProvider(Seq(""))
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted with import only" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      val application           =
        applicationBuilder(userAnswers = Some(importOnlyAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      running(application) {
        val request =
          FakeRequest(POST, declarationDateRoute)
            .withFormUrlEncodedBody(("value", DeclarationDate.values.head.toString))
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, declarationDateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val form = formProvider(Seq(""))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DeclarationDateView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, "")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, declarationDateRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, declarationDateRoute)
            .withFormUrlEncodedBody(("value", DeclarationDate.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
