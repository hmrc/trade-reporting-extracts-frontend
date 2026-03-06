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
import config.FrontendAppConfig
import models.report.{EmailSelection, ReportConfirmation, SubmissionMeta}
import pages.report.{EmailSelectionPage, NewEmailNotificationPage}
import play.api.i18n.Messages
import play.api.i18n.Messages.implicitMessagesProviderToMessages
import play.api.inject
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.DateTimeFormats
import views.html.report.RequestConfirmationView

import java.time.{Clock, Instant, ZoneId}

class RequestConfirmationControllerSpec extends SpecBase {

  private val fixedInstant: Instant = Instant.parse("2025-05-05T10:15:30Z")
  private val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

  "RequestConfirmationController" - {

    "return OK and render the correct view when submissionMeta and EmailSelectionPage are defined when export" in {
      val newEmail          = "new.email@example.com"
      val selectedEmails    = Seq("email1@example.com", "email2@example.com", newEmail)
      val emailString       = selectedEmails.mkString(", ")
      val notificationEmail = "notify@example.com"

      val submissionMetaJson: JsObject = Json
        .toJson(
          SubmissionMeta(
            reportConfirmations = Seq(ReportConfirmation("MyReport", "exportItem", "RE00000001")),
            submittedAt = Instant.now(fixedClock),
            isMoreThanOneReport = false,
            allEmails = Seq(notificationEmail) ++ selectedEmails
          )
        )
        .as[JsObject]

      val userAnswers = emptyUserAnswers
        .set(EmailSelectionPage, selectedEmails.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, newEmail)
        .success
        .value
        .copy(submissionMeta = Some(submissionMetaJson))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl
        val view      = application.injector.instanceOf[RequestConfirmationView]

        val (expectedDate, expectedTime) =
          DateTimeFormats.instantToDateAndTime(Instant.now(fixedClock), fixedClock)(messages(application))

        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          Seq(notificationEmail) ++ selectedEmails,
          false,
          Seq(ReportConfirmation("MyReport", "reportTypeImport.exportItem", "RE00000001")),
          surveyUrl,
          expectedDate,
          expectedTime
        )(request, messages(application)).toString

        contentAsString(result) must include("MyReport")
        contentAsString(result) must include("RE00000001")
        contentAsString(result) must include(notificationEmail)
        contentAsString(result) must include(
          "We’re processing your request"
        )

      }
    }

    "return OK and render the correct view when submissionMeta and EmailSelectionPage are defined when import" in {
      val newEmail          = "new.email@example.com"
      val selectedEmails    = Seq("email1@example.com", "email2@example.com", newEmail)
      val emailString       = selectedEmails.mkString(", ")
      val notificationEmail = "notify@example.com"

      val submissionMetaJson: JsObject = Json
        .toJson(
          SubmissionMeta(
            reportConfirmations = Seq(
              ReportConfirmation("MyReport", "importItem", "RE00000001"),
              ReportConfirmation("MyReport", "importHeader", "RE00000002"),
              ReportConfirmation("MyReport", "importTaxLine", "RE00000003")
            ),
            submittedAt = Instant.now(fixedClock),
            isMoreThanOneReport = true,
            allEmails = Seq(notificationEmail) ++ selectedEmails
          )
        )
        .as[JsObject]

      val userAnswers = emptyUserAnswers
        .set(EmailSelectionPage, selectedEmails.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, "test@test.com")
        .success
        .value
        .copy(submissionMeta = Some(submissionMetaJson))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val surveyUrl = appConfig.exitSurveyUrl
        val view      = application.injector.instanceOf[RequestConfirmationView]

        val (expectedDate, expectedTime) =
          DateTimeFormats.instantToDateAndTime(Instant.now(fixedClock), fixedClock)(messages(application))

        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          Seq(notificationEmail) ++ selectedEmails,
          true,
          Seq(
            ReportConfirmation("MyReport", "reportTypeImport.importItem", "RE00000001"),
            ReportConfirmation("MyReport", "reportTypeImport.importHeader", "RE00000002"),
            ReportConfirmation("MyReport", "reportTypeImport.importTaxLine", "RE00000003")
          ),
          surveyUrl,
          expectedDate,
          expectedTime
        )(request, messages(application)).toString

        contentAsString(result) must include("MyReport")
        contentAsString(result) must include("RE00000001")
        contentAsString(result) must include("RE00000002")
        contentAsString(result) must include("RE00000003")
        contentAsString(result) must include("Import item")
        contentAsString(result) must include("Import header")
        contentAsString(result) must include("Import tax line")
        contentAsString(result) must include(notificationEmail)
        contentAsString(result) must include(
          "We’re processing your request"
        )
      }
    }

    "return OK and render plural message when isMoreThanOneReport is true" in {
      val newEmail          = "new.email@example.com"
      val selectedEmails    = Seq("email1@example.com", "email2@example.com", newEmail)
      val emailString       = selectedEmails.mkString(", ")
      val notificationEmail = "notify@example.com"

      val submissionMetaJson: JsObject = Json
        .toJson(
          SubmissionMeta(
            reportConfirmations = Seq(ReportConfirmation("MyReport", "importTaxLine", "RE00000001")),
            submittedAt = Instant.now(fixedClock),
            isMoreThanOneReport = true,
            allEmails = Seq(notificationEmail) ++ selectedEmails
          )
        )
        .as[JsObject]

      val userAnswers = emptyUserAnswers
        .set(EmailSelectionPage, selectedEmails.toSet)
        .success
        .value
        .set(NewEmailNotificationPage, newEmail)
        .success
        .value
        .copy(submissionMeta = Some(submissionMetaJson))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(inject.bind[Clock].toInstance(fixedClock))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(
          "We’re processing your request"
        )
      }
    }

    "return OK and render empty values when submissionMeta is missing" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.report.routes.RequestConfirmationController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("")
      }
    }
  }
}
