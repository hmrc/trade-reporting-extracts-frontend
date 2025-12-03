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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, ok, post, serverError, urlEqualTo}
import exceptions.NoAuthorisedUserFoundException
import models.ConsentStatus.Granted
import models.{AuditDownloadRequest, CompanyInformation, NotificationEmail, ThirdPartyDetails, UserActiveStatus, UserDetails}
import models.report.{ReportConfirmation, ReportRequestUserAnswersModel}
import models.thirdparty.{AccountAuthorityOverViewModel, EditThirdPartyRequest, ThirdPartyAddedConfirmation, ThirdPartyRequest}
import org.apache.pekko.Done
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import com.fasterxml.jackson.core.JsonParseException
import java.time.*

class TradeReportingExtractsConnectorSpec
    extends SpecBase
    with ScalaFutures
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.trade-reporting-extracts.port" -> server.port)
      .build()

  "TradeReportingExtractsConnector" - {

    "createReportRequest" - {

      val url = "/trade-reporting-extracts/create-report-request"

      "must return an OK response single request confirmation when JSON is valid" in {

        val responseBody =
          s"""[{
             |  "reportName": "name1",
             |  "reportType": "importHeader",
             |  "reportReference": "RE00000001"
             |}]""".stripMargin

        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(ok(responseBody))
          )

          val result = connector
            .createReportRequest(
              ReportRequestUserAnswersModel(
                eori = "eori",
                dataType = "import",
                whichEori = Some("eori"),
                eoriRole = Set("declarant"),
                reportType = Set("importHeader"),
                reportStartDate = "2025-04-16",
                reportEndDate = "2025-05-16",
                reportName = "MyReport",
                additionalEmail = Some(Set("email@email.com"))
              )
            )
            .futureValue

          result mustBe Seq(ReportConfirmation("name1", "importHeader", "RE00000001"))
        }
      }

      "must return an OK response multiple request confirmations when JSON is valid" in {

        val responseBody =
          s"""[
             |  {
             |    "reportName": "name1",
             |    "reportType": "importHeader",
             |    "reportReference": "RE00000001"
             |  },
             |  {
             |    "reportName": "name1",
             |    "reportType": "importItem",
             |    "reportReference": "RE00000002"
             |  }
             |]""".stripMargin

        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(ok(responseBody))
          )

          val result = connector
            .createReportRequest(
              ReportRequestUserAnswersModel(
                eori = "eori",
                dataType = "import",
                whichEori = Some("eori"),
                eoriRole = Set("declarant"),
                reportType = Set("importHeader", "importItem"),
                reportStartDate = "2025-04-16",
                reportEndDate = "2025-05-16",
                reportName = "name1",
                additionalEmail = Some(Set("email@email.com"))
              )
            )
            .futureValue

          result mustBe Seq(
            ReportConfirmation("name1", "importHeader", "RE00000001"),
            ReportConfirmation("name1", "importItem", "RE00000002")
          )
        }
      }

      "must return a failed future when OK and JSON not valid" in {

        val responseBody =
          s"""{
             |  "ref" : [ "TR-00000001" ]
             |}""".stripMargin

        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(ok(responseBody))
          )

          val result = connector
            .createReportRequest(
              ReportRequestUserAnswersModel(
                eori = "eori",
                dataType = "import",
                whichEori = Some("eori"),
                eoriRole = Set("declarant"),
                reportType = Set("importHeader"),
                reportStartDate = "2025-04-16",
                reportEndDate = "2025-05-16",
                reportName = "MyReport",
                additionalEmail = Some(Set("email@email.com"))
              )
            )
            .failed
            .futureValue

          result mustBe an[uk.gov.hmrc.http.UpstreamErrorResponse]
        }
      }

      "must return a failed future when anything but OK" in {

        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(aResponse.withStatus(500))
          )

          val result = connector
            .createReportRequest(
              ReportRequestUserAnswersModel(
                eori = "eori",
                dataType = "import",
                whichEori = Some("eori"),
                eoriRole = Set("declarant"),
                reportType = Set("importHeader"),
                reportStartDate = "2025-04-16",
                reportEndDate = "2025-05-16",
                reportName = "MyReport",
                additionalEmail = Some(Set("email@email.com"))
              )
            )
            .failed
            .futureValue

          result mustBe an[uk.gov.hmrc.http.UpstreamErrorResponse]
        }
      }
    }

    "hasReachedSubmissionLimit" - {

      val eori = "GB123456789000"
      val url  = s"/trade-reporting-extracts/report-submission-limit/$eori"

      "must return false when response is NO_CONTENT (204)" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .get(
                WireMock.urlEqualTo(url)
              )
              .willReturn(
                WireMock.aResponse().withStatus(204)
              )
          )
          val result    = connector.hasReachedSubmissionLimit(eori).futureValue
          result mustBe false
        }
      }

      "must return true when response is TOO_MANY_REQUESTS (429)" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .get(
                WireMock.urlEqualTo(url)
              )
              .willReturn(
                WireMock.aResponse().withStatus(429)
              )
          )
          val result    = connector.hasReachedSubmissionLimit(eori).futureValue
          result mustBe true
        }
      }

      "must throw a RuntimeException for unexpected status" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .get(
                WireMock.urlEqualTo(url)
              )
              .willReturn(
                WireMock.aResponse().withStatus(500)
              )
          )
          val thrown    = intercept[RuntimeException] {
            connector.hasReachedSubmissionLimit(eori).futureValue
          }
          thrown.getMessage must include("Unexpected response: 500")
        }
      }
    }

    "getUserDetails" - {

      "must return user details when the API call is successful" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          val eori                = "GB123456789000"
          val expectedUserDetails = UserDetails(
            eori = eori,
            additionalEmails = Seq.empty,
            authorisedUsers = Seq.empty,
            companyInformation = CompanyInformation(
              name = "Test Company",
              consent = Granted
            ),
            notificationEmail = NotificationEmail("test@test.com", LocalDateTime.now())
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/trade-reporting-extracts/eori/get-user-detail"))
              .withRequestBody(equalToJson(s"""{ "eori": "$eori" }"""))
              .willReturn(
                ok(Json.toJson(expectedUserDetails).toString())
              )
          )

          val result = connector.getUserDetails(eori).futureValue
          result mustBe expectedUserDetails
        }
      }

      "must throw an exception when the API call fails" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          val eori      = "GB123456789000"
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/trade-reporting-extracts/eori/get-user-detail"))
              .withRequestBody(equalToJson(s"""{ "eori": "$eori" }"""))
              .willReturn(
                aResponse().withStatus(500).withBody("Failed to fetch getUserDetails")
              )
          )
          val thrown    = intercept[RuntimeException] {
            connector.getUserDetails(eori).futureValue
          }
          thrown.getMessage must include("Failed to fetch getUserDetails")
        }
      }
    }

    "auditReportDownload" - {

      val url         = "/trade-reporting-extracts/downloaded-audit"
      val request     = AuditDownloadRequest(
        reportReference = "some-reference",
        fileName = "report.csv",
        fileUrl = "http://localhost/report.csv"
      )
      val requestBody = Json.toJson(request).toString()

      "must return true when the API call is successful (NO_CONTENT)" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(requestBody))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )

          val result = connector.auditReportDownload(request).futureValue

          result mustBe true
        }
      }

      "must return false for any other status" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(requestBody))
              .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
          )

          val result = connector.auditReportDownload(request).futureValue

          result mustBe false
        }
      }
    }

    "getReportRequestLimitNumber" - {

      "must return the report request limit number when the API call is successful" in {
        val app = application
        running(app) {
          val connector           = app.injector.instanceOf[TradeReportingExtractsConnector]
          val expectedLimitNumber = "25"

          server.stubFor(
            WireMock
              .get(WireMock.urlEqualTo("/trade-reporting-extracts/report-request-limit-number"))
              .willReturn(WireMock.ok("\"25\""))
          )

          val result = connector.getReportRequestLimitNumber.futureValue
          result mustBe expectedLimitNumber
        }
      }

      "must throw an exception when the API call fails" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo("/trade-reporting-extracts/report-request-limit-number"))
              .willReturn(
                aResponse().withStatus(500).withBody("error")
              )
          )

          val thrown = intercept[RuntimeException] {
            connector.getReportRequestLimitNumber.futureValue
          }
          thrown.getMessage must include("error")
        }
      }
    }

    "createThirdPartyAddRequest" - {

      val url = "/trade-reporting-extracts/add-third-party-request"

      val thirdPartyRequest = ThirdPartyRequest(
        userEORI = "GB1",
        thirdPartyEORI = "GB2",
        accessStart = Instant.parse("2024-01-01T00:00:00Z"),
        accessEnd = None,
        reportDateStart = None,
        reportDateEnd = None,
        accessType = Set("IMPORT"),
        referenceName = None
      )

      val confirmationJson =
        s"""{
           |  "thirdPartyEori": "GB987654321000"
           |}""".stripMargin

      "must return confirmation when response is OK and JSON is valid" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(ok(confirmationJson))
          )

          val result = connector.createThirdPartyAddRequest(thirdPartyRequest).futureValue
          result.thirdPartyEori mustBe "GB987654321000"
        }
      }

      "must fail when response is OK but JSON is invalid" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(ok("""{ "unexpected": "value" }"""))
          )

          val result = connector.createThirdPartyAddRequest(thirdPartyRequest).failed.futureValue
          result mustBe an[uk.gov.hmrc.http.UpstreamErrorResponse]
        }
      }

      "must fail when response status is not OK" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(aResponse().withStatus(500))
          )
          val result    = connector.createThirdPartyAddRequest(thirdPartyRequest).failed.futureValue
          result mustBe an[uk.gov.hmrc.http.UpstreamErrorResponse]
        }
      }
    }

    "getThirdPartyDetails" - {

      val validThirdPartyDetails = ThirdPartyDetails(
        Some("reference"),
        LocalDate.of(2025, 1, 1),
        Some(LocalDate.of(2025, 12, 31)),
        Set("import"),
        Some(LocalDate.of(2025, 1, 1)),
        Some(LocalDate.of(2025, 12, 31))
      )

      "must return third party details when OK and valid third party details" in {

        val response = Json.toJson(validThirdPartyDetails).toString()
        val app      = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo("/trade-reporting-extracts/third-party-details"))
              .withRequestBody(equalToJson("""{ "eori": "123", "thirdPartyEori": "456" }"""))
              .willReturn(
                aResponse().withStatus(OK).withBody(response)
              )
          )
          val result = connector.getThirdPartyDetails("123", "456").futureValue
          result mustBe validThirdPartyDetails
        }
      }

      "must return failed future when invalid json received" in {

        val invalidResponse = """{ "foo": "bar" }"""

        val response = Json.toJson(validThirdPartyDetails).toString()
        val app      = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo("/trade-reporting-extracts/third-party-details"))
              .withRequestBody(equalToJson("""{ "eori": "123", "thirdPartyEori": "456" }"""))
              .willReturn(
                aResponse().withStatus(OK).withBody(invalidResponse)
              )
          )
          val result = connector.getThirdPartyDetails("123", "456").failed.futureValue
          result mustBe an[UpstreamErrorResponse]
        }
      }

      "must return failed future when not OK received" in {

        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo("/trade-reporting-extracts/third-party-details"))
              .withRequestBody(equalToJson("""{ "eori": "123", "thirdPartyEori": "456" }"""))
              .willReturn(
                aResponse().withStatus(500).withBody("error")
              )
          )
          val result = connector.getThirdPartyDetails("123", "456").failed.futureValue
          result mustBe an[UpstreamErrorResponse]
        }
      }
    }

    "selfRemoveThirdPartyAccess" - {
      "Return Done when OK received" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .delete(urlEqualTo("/trade-reporting-extracts/third-party-access-self-removal"))
              .withRequestBody(equalToJson("""{ "traderEori": "123", "thirdPartyEori": "456" }"""))
              .willReturn(
                aResponse().withStatus(OK)
              )
          )
          val result = connector.selfRemoveThirdPartyAccess("123", "456").futureValue
          result mustBe Done
        }
      }

      "Return upstream error response when anything else received" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .delete(urlEqualTo("/trade-reporting-extracts/third-party-access-self-removal"))
              .withRequestBody(equalToJson("""{ "traderEori": "123", "thirdPartyEori": "456" }"""))
              .willReturn(
                aResponse().withStatus(500).withBody("error")
              )
          )
          val result = connector.selfRemoveThirdPartyAccess("123", "456").failed.futureValue
          result mustBe an[UpstreamErrorResponse]
        }
      }
    }

    "removeThirdParty" - {
      val url            = "/trade-reporting-extracts/remove-third-party"
      val eori           = "GB123"
      val thirdPartyEori = "GB456"

      "must return Done when response is NO_CONTENT" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .delete(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "eori": "$eori", "thirdPartyEori": "$thirdPartyEori" }"""))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )
          val result    = connector.removeThirdParty(eori, thirdPartyEori).futureValue
          result mustBe Done
        }
      }

      "must fail with UpstreamErrorResponse when response is not NO_CONTENT" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          server.stubFor(
            WireMock
              .delete(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "eori": "$eori", "thirdPartyEori": "$thirdPartyEori" }"""))
              .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("error"))
          )
          val result    = connector.removeThirdParty(eori, thirdPartyEori).failed.futureValue
          result mustBe an[UpstreamErrorResponse]
        }
      }
    }

    "getAccountsAuthorityOver" - {
      val url  = "/trade-reporting-extracts/get-users-by-authorised-eori"
      val eori = "GB123456789000"

      val expectedResponse = Seq(
        AccountAuthorityOverViewModel("GB111", Some("Business One"), Some(UserActiveStatus.Active)),
        AccountAuthorityOverViewModel("GB222", None, Some(UserActiveStatus.Upcoming))
      )

      "must return list of AccountAuthorityOverViewModel when API call is successful" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$eori" }"""))
              .willReturn(ok(Json.toJson(expectedResponse).toString()))
          )

          val result = connector.getAccountsAuthorityOver(eori).futureValue
          result mustBe expectedResponse
        }
      }

      "must throw exception when API call fails" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$eori" }"""))
              .willReturn(serverError().withBody("error"))
          )

          val thrown = intercept[RuntimeException] {
            connector.getAccountsAuthorityOver(eori).futureValue
          }
          thrown.getMessage must include("error")
        }
      }
    }

    "getSelectThirdPartyEori" - {
      val url  = "/trade-reporting-extracts/get-users-by-authorised-eori-date-filtered"
      val eori = "GB123456789000"

      val expectedResponse = Seq(
        AccountAuthorityOverViewModel("GB333", Some("Business Three"), None),
        AccountAuthorityOverViewModel("GB444", Some("Business Four"), None)
      )

      "must return list of AccountAuthorityOverViewModel when API call is successful" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$eori" }"""))
              .willReturn(ok(Json.toJson(expectedResponse).toString()))
          )

          val result = connector.getSelectThirdPartyEori(eori).futureValue
          result mustBe expectedResponse
        }
      }

      "must throw exception when API call fails" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$eori" }"""))
              .willReturn(aResponse().withStatus(500).withBody("some error"))
          )

          val thrown = intercept[RuntimeException] {
            connector.getSelectThirdPartyEori(eori).futureValue
          }
          thrown.getMessage must include("some error")
        }
      }
    }

    "editThirdPartyRequest" - {
      val url                   = "/trade-reporting-extracts/edit-third-party-request"
      val editThirdPartyRequest = ThirdPartyRequest(
        "123",
        "456",
        Instant.now(),
        None,
        None,
        None,
        Set("IMPORT"),
        Some("newRef")
      )

      "must return confirmation when successful" in {
        val app              = application
        val expectedResponse = ThirdPartyAddedConfirmation("456")
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .put(urlEqualTo(url))
              .withRequestBody(equalToJson(Json.toJson(editThirdPartyRequest).toString()))
              .willReturn(ok(Json.toJson(expectedResponse).toString))
          )

          val result = connector.editThirdPartyRequest(editThirdPartyRequest).futureValue
          result mustBe expectedResponse
        }
      }

      "must return error when confirmation can't be parsed" in {
        val app                = application
        val unexpectedResponse = "foo"

        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .put(urlEqualTo(url))
              .withRequestBody(equalToJson(Json.toJson(editThirdPartyRequest).toString()))
              .willReturn(ok(Json.toJson(unexpectedResponse).toString))
          )

          val result = connector.editThirdPartyRequest(editThirdPartyRequest).failed.futureValue
          result mustBe an[UpstreamErrorResponse]
        }

      }

      "must return error when anything but OK" in {

        val app = application

        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .put(urlEqualTo(url))
              .withRequestBody(equalToJson(Json.toJson(editThirdPartyRequest).toString()))
              .willReturn(aResponse().withStatus(500).withBody("some error"))
          )

          val result = connector.editThirdPartyRequest(editThirdPartyRequest).failed.futureValue
          result mustBe an[UpstreamErrorResponse]
        }

      }

    }

    "getAuthorisedBusinessDetails" - {
      val url            = "/trade-reporting-extracts/authorised-business-details"
      val thirdPartyEori = "GB123456789000"
      val traderEori     = "GB111111111111"

      val validThirdPartyDetails = ThirdPartyDetails(
        referenceName = Some("Test Company"),
        accessStartDate = LocalDate.of(2025, 1, 1),
        accessEndDate = Some(LocalDate.of(2025, 12, 31)),
        dataTypes = Set("imports"),
        dataStartDate = Some(LocalDate.of(2024, 6, 1)),
        dataEndDate = Some(LocalDate.of(2024, 12, 31))
      )

      "must return ThirdPartyDetails when API returns 200 with valid JSON" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$thirdPartyEori", "traderEori": "$traderEori" }"""))
              .willReturn(ok(Json.toJson(validThirdPartyDetails).toString()))
          )

          val result = connector.getAuthorisedBusinessDetails(thirdPartyEori, traderEori).futureValue
          result mustBe validThirdPartyDetails
        }
      }

      "must throw NoAuthorisedUserFoundException when API returns 404" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$thirdPartyEori", "traderEori": "$traderEori" }"""))
              .willReturn(aResponse().withStatus(NOT_FOUND).withBody("No authorised user found for third party EORI:"))
          )

          val result = connector.getAuthorisedBusinessDetails(thirdPartyEori, traderEori).failed.futureValue
          result mustBe a[NoAuthorisedUserFoundException]
          result.getMessage mustBe "No authorised user found for third party EORI:"
        }
      }

      "must throw UpstreamErrorResponse when API returns other error status" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$thirdPartyEori", "traderEori": "$traderEori" }"""))
              .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR).withBody("Server error"))
          )

          val result = connector.getAuthorisedBusinessDetails(thirdPartyEori, traderEori).failed.futureValue
          result mustBe an[UpstreamErrorResponse]
          result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe INTERNAL_SERVER_ERROR
          result.asInstanceOf[UpstreamErrorResponse].message must include(
            "Unexpected response from /trade-reporting-extracts/authorised-business-details"
          )
        }
      }

      "must throw JsonParseException when JSON parsing fails" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          server.stubFor(
            WireMock
              .get(urlEqualTo(url))
              .withRequestBody(equalToJson(s"""{ "thirdPartyEori": "$thirdPartyEori", "traderEori": "$traderEori" }"""))
              .willReturn(ok("invalid json"))
          )

          val result = connector.getAuthorisedBusinessDetails(thirdPartyEori, traderEori).failed.futureValue
          result mustBe an[JsonParseException]
        }
      }
    }

  }
}
