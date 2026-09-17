/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.contact

import base.SpecBase
import forms.contact.MaybeToggleEmailNotificationsFormProvider
import models.{NotificationEmail, UserDetails}
import navigation.{FakeNavigator, Navigator}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.contact.MaybeToggleEmailNotificationsView

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class MaybeToggleEmailNotificationsControllerSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def onwardRouteEnable = Call("GET", "/request-customs-declaration-data/email-notifications-enabled")

  def onwardRouteDisable = Call("GET", "/request-customs-declaration-data/email-notifications-disabled")

  val formProvider = new MaybeToggleEmailNotificationsFormProvider()
  val form         = formProvider(true)

  lazy val maybeToggleEmailNotificationsRouteDisablingEmail =
    controllers.contact.routes.MaybeToggleEmailNotificationsController.onPageLoad(false).url

  lazy val maybeToggleEmailNotificationsRouteEnablingEmail =
    controllers.contact.routes.MaybeToggleEmailNotificationsController.onPageLoad(true).url

  "MaybeToggleEmailNotifications Controller" - {

    "must return OK and the correct view for a GET when disabling email" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            true
          )
        )
      )

      running(application) {
        val request = FakeRequest(GET, maybeToggleEmailNotificationsRouteDisablingEmail)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaybeToggleEmailNotificationsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, false, "example@example.com")(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when enabling email" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            false
          )
        )
      )

      running(application) {
        val request = FakeRequest(GET, maybeToggleEmailNotificationsRouteEnablingEmail)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaybeToggleEmailNotificationsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, true, "example@example.com")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted when disabling notifications" in {

      val mockSessionRepository             = mock[SessionRepository]
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            true
          )
        )
      )

      when(
        mockTradeReportingExtractsService.updatePersonalEmailNotificationsPreference(
          any()
        )(any())
      ).thenReturn(
        Future.successful(Done)
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteDisable)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, maybeToggleEmailNotificationsRouteDisablingEmail)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteDisable.url
      }
    }

    "must redirect to the next page when valid data is submitted when enabling notifications" in {

      val mockSessionRepository             = mock[SessionRepository]
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            false
          )
        )
      )

      when(
        mockTradeReportingExtractsService.updatePersonalEmailNotificationsPreference(
          any()
        )(any())
      ).thenReturn(
        Future.successful(Done)
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteEnable)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, maybeToggleEmailNotificationsRouteEnablingEmail)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRouteEnable.url
      }
    }

    "must redirect to contact details page when user decides against continuing with change " in {

      val mockSessionRepository             = mock[SessionRepository]
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            false
          )
        )
      )

      when(
        mockTradeReportingExtractsService.updatePersonalEmailNotificationsPreference(
          any()
        )(any())
      ).thenReturn(
        Future.successful(Done)
      )

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRouteEnable)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, maybeToggleEmailNotificationsRouteEnablingEmail)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.contact.routes.ContactDetailsController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val form = formProvider(false)

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            true
          )
        )
      )

      running(application) {
        val request =
          FakeRequest(POST, maybeToggleEmailNotificationsRouteDisablingEmail)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MaybeToggleEmailNotificationsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, false, "example@example.com")(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to error page when enabling emails when already enabled" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            false
          )
        )
      )

      running(application) {
        val request = FakeRequest(GET, maybeToggleEmailNotificationsRouteDisablingEmail)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.EmailPreferenceToggleErrorController
          .onPageLoad()
          .url

      }

    }

    "must redirect to error page when disabling email when already disabled" in {

      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
        )
        .build()

      when(mockTradeReportingExtractsService.getUserDetails(any())(any())).thenReturn(
        Future.successful(
          UserDetails(
            null,
            null,
            null,
            null,
            NotificationEmail("example@example.com", LocalDateTime.of(2024, 1, 1, 0, 0, 0), false),
            true
          )
        )
      )

      running(application) {
        val request = FakeRequest(GET, maybeToggleEmailNotificationsRouteEnablingEmail)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.EmailPreferenceToggleErrorController
          .onPageLoad()
          .url
      }

    }
  }
}
