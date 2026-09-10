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
import controllers.routes
import models.{NotificationEmail, UserDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages.implicitMessagesProviderToMessages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import utils.DateTimeFormats
import views.html.contact.ToggledEmailNotificationsConfirmationView
import play.api.i18n.Messages
import play.api.inject.bind

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class ToggledEmailNotificationsConfirmationControllerSpec extends SpecBase with MockitoSugar {

  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private implicit val messages: Messages = stubMessages()

  "ToggledEmailNotificationsConfirmation Controller" - {

    val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]

    "must return OK and the correct view for a GET when user enabled emails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.contact.routes.ToggledEmailNotificationsConfirmationController.onPageLoad(true).url
        )

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

        val result = route(application, request).value

        val view = application.injector.instanceOf[ToggledEmailNotificationsConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          true,
          "example@example.com",
          DateTimeFormats.dateFormatter(LocalDate.now)
        )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when user disabled emails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService))
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.contact.routes.ToggledEmailNotificationsConfirmationController.onPageLoad(false).url
        )

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

        val result = route(application, request).value

        val view = application.injector.instanceOf[ToggledEmailNotificationsConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          false,
          "example@example.com",
          DateTimeFormats.dateFormatter(LocalDate.now)
        )(request, messages(application)).toString
      }
    }
  }
}
