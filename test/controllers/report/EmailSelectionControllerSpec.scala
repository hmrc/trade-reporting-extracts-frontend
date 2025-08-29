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
import forms.report.EmailSelectionFormProvider
import models.ConsentStatus.Granted
import models.report.EmailSelection
import models.{CompanyInformation, NormalMode, NotificationEmail, UserAnswers, UserDetails}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.report.EmailSelectionPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.report.EmailSelectionView

import java.time.LocalDateTime
import scala.concurrent.Future

class EmailSelectionControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute: Call =
    Call("GET", "/request-customs-declaration-data/new-notification-email")

  private lazy val emailSelectionRoute: String =
    controllers.report.routes.EmailSelectionController.onPageLoad(NormalMode).url

  private val formProvider    = new EmailSelectionFormProvider()
  private val dynamicEmails   = Seq("user1@example.com", "user2@example.com")
  private val mockUserDetails = UserDetails(
    eori = "GB123456789000",
    additionalEmails = dynamicEmails,
    authorisedUsers = Seq.empty,
    companyInformation = CompanyInformation(
      name = "Test Ltd",
      consent = Granted
    ),
    notificationEmail = NotificationEmail("user@example.com", LocalDateTime.now())
  )

  private val form = formProvider(dynamicEmails)

  private val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
  implicit val hc: HeaderCarrier                = HeaderCarrier()
  when(mockTradeReportingExtractsService.setupUser(any())(any()))
    .thenReturn(Future.successful(mockUserDetails.copy(additionalEmails = dynamicEmails)))

  "EmailSelection Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, emailSelectionRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[EmailSelectionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, dynamicEmails)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, emailSelectionRoute)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[EmailSelectionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(Set(EmailSelection.AddNewEmailValue)),
          NormalMode,
          dynamicEmails
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, emailSelectionRoute)
          .withFormUrlEncodedBody("value[0]" -> EmailSelection.AddNewEmail.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(POST, emailSelectionRoute)
          .withFormUrlEncodedBody("value[0]" -> "invalid value")

        val boundForm = form.bind(Map("value[0]" -> "invalid value"))
        val view      = application.injector.instanceOf[EmailSelectionView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, dynamicEmails)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(GET, emailSelectionRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(POST, emailSelectionRoute)
          .withFormUrlEncodedBody("value[0]" -> EmailSelection.AddNewEmail.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
