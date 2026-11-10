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

import base.AppPerSuiteSpecBase
import models.ConsentStatus.Granted
import models.{CompanyInformation, NotificationEmail, UserDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.contact.ContactDetailsView

import java.time.LocalDateTime
import scala.concurrent.Future

class ContactDetailsControllerSpec extends AppPerSuiteSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
  private val companyInformation: CompanyInformation     =
    CompanyInformation(
      name = "ABC Company",
      consent = Granted,
      inactiveEori = false
    )
  private val eori                                       = "GB123456789002"

  override def fakeApplication() =
    applicationBuilder(useSharedFakeAction = true)
      .overrides(bind[TradeReportingExtractsService].toInstance(mockService))
      .build()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    org.mockito.Mockito.reset(mockService)
  }

  "ContactDetailsController" - {

    val manageEmailGuideUrl: String =
      "https://www.gov.uk/guidance/manage-your-email-address-for-the-customs-declaration-service"

    "must return OK and render the correct view for a GET request" in {
      setUserAnswers(None)
      val application = app

      val userDetails = UserDetails(
        eori = eori,
        additionalEmails = Seq("test@example.com"),
        authorisedUsers = Seq.empty,
        companyInformation = companyInformation,
        notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now(), false),
        personalEmailNotificationsEnabled = true
      )

      when(mockService.getUserDetails(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userDetails))

      val request = FakeRequest(GET, controllers.contact.routes.ContactDetailsController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[ContactDetailsView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        companyInformation,
        eori,
        "notify@example.com",
        Seq("test@example.com"),
        true,
        manageEmailGuideUrl,
        false,
        false
      )(
        request,
        messages(application)
      ).toString
    }

    "must return OK and render the correct view for when a user has no valid email address" in {
      setUserAnswers(None)
      val application = app

      val userDetails = UserDetails(
        eori = eori,
        additionalEmails = Seq("test@example.com"),
        authorisedUsers = Seq.empty,
        companyInformation = companyInformation,
        notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now(), true),
        personalEmailNotificationsEnabled = true
      )

      when(mockService.getUserDetails(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userDetails))

      val request = FakeRequest(GET, controllers.contact.routes.ContactDetailsController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[ContactDetailsView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        companyInformation,
        eori,
        "notify@example.com",
        Seq("test@example.com"),
        true,
        manageEmailGuideUrl,
        false,
        true
      )(
        request,
        messages(application)
      ).toString
      contentAsString(result) must include(
        "We are unable to display your registered email address because there is no verified email address."
      )
    }

    "must return OK and render the correct view for when a user has an inactive eori" in {
      setUserAnswers(None)
      val application = app

      val userDetails = UserDetails(
        eori = eori,
        additionalEmails = Seq("test@example.com"),
        authorisedUsers = Seq.empty,
        companyInformation = companyInformation.copy(inactiveEori = true),
        notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now(), false),
        personalEmailNotificationsEnabled = true
      )

      when(mockService.getUserDetails(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userDetails))

      val request = FakeRequest(GET, controllers.contact.routes.ContactDetailsController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[ContactDetailsView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        companyInformation,
        eori,
        "notify@example.com",
        Seq("test@example.com"),
        true,
        manageEmailGuideUrl,
        true,
        false
      )(
        request,
        messages(application)
      ).toString
      contentAsString(result) must include("Your EORI number is inactive")
    }

    "must return OK and render the correct view for when a user has an inactive eori and no available email address" in {
      setUserAnswers(None)
      val application = app

      val userDetails = UserDetails(
        eori = eori,
        additionalEmails = Seq("test@example.com"),
        authorisedUsers = Seq.empty,
        companyInformation = companyInformation.copy(inactiveEori = true),
        notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now(), true),
        personalEmailNotificationsEnabled = true
      )

      when(mockService.getUserDetails(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userDetails))

      val request = FakeRequest(GET, controllers.contact.routes.ContactDetailsController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[ContactDetailsView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        companyInformation,
        eori,
        "notify@example.com",
        Seq("test@example.com"),
        true,
        manageEmailGuideUrl,
        true,
        true
      )(
        request,
        messages(application)
      ).toString
      contentAsString(result) must include("Your EORI number is inactive")
    }

    "must return OK and render the correct view for a GET request when user has reached additional email limit of 5" in {

      val existingEmails = Seq(
        "existing1@example.com",
        "existing2@example.com",
        "existing3@example.com",
        "existing4@example.com",
        "existing5@example.com"
      )

      setUserAnswers(None)
      val application = app

      val userDetails = UserDetails(
        eori = eori,
        additionalEmails = existingEmails,
        authorisedUsers = Seq.empty,
        companyInformation = companyInformation,
        notificationEmail = NotificationEmail("notify@example.com", LocalDateTime.now(), false),
        personalEmailNotificationsEnabled = true
      )

      when(mockService.getUserDetails(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(userDetails))

      val request = FakeRequest(GET, controllers.contact.routes.ContactDetailsController.onPageLoad().url)
      val result  = route(application, request).value

      val view = application.injector.instanceOf[ContactDetailsView]

      status(result) mustEqual OK
      val body = contentAsString(result)
      val msgs = messages(application)
      body mustEqual view(
        companyInformation,
        eori,
        "notify@example.com",
        existingEmails,
        true,
        manageEmailGuideUrl,
        false,
        false
      )(
        request,
        msgs
      ).toString

      body must include(msgs("additionalEmail.limitReached"))
      val addUrl = controllers.contact.routes.NewAdditionalEmailController.onPageLoad().url
      body must not include addUrl

    }
  }

}
