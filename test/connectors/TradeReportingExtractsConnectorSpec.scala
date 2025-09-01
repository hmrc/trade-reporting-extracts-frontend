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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, ok, post, urlEqualTo}
import models.{AuditDownloadRequest, CompanyInformation, NotificationEmail, UserDetails}
import models.report.ReportRequestUserAnswersModel
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.Future

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

    "getEoriList" - {

      "must return a list of EORI numbers when the file is read successfully" in {
        val app = application

        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          val result    = connector.getEoriList().futureValue

          result mustBe Seq("Eori1", "Eori2", "Eori3")
        }
      }

      "must log an error and return a failed future if the file cannot be read" in {
        val app = application
        running(app) {
          val mockConnector = mock[TradeReportingExtractsConnector]
          when(mockConnector.getEoriList())
            .thenReturn(Future.failed(new RuntimeException("Failed to read or parse EORI list from file")))

          val thrown = intercept[RuntimeException] {
            mockConnector.getEoriList().futureValue
          }

          thrown.getMessage must include("Failed to read or parse EORI list from file")
        }
      }

      "must return an empty sequence if the JSON is invalid or empty" in {
        val app = application

        val path = "conf/resources/emptyEoriList.json"
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          val result    = connector.getEoriList(path).futureValue

          result mustBe Seq.empty
        }
      }
    }

    "createReportRequest" - {

      val url = "/trade-reporting-extracts/create-report-request"

      "must return an OK response and reference when JSON is valid" in {

        val responseBody =
          s"""{
             |  "references" : [ "TR-00000001" ]
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
            .futureValue

          result mustBe Seq("TR-00000001")
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
              consent = "1"
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
  }
}
