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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.TradeReportingExtractsConnector
import models.{AddressInformation, CompanyInformation, NotificationEmail, UserDetails}
import models.report.ReportRequestUserAnswersModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class TradeReportingExtractsServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with Matchers {

  implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrier()

  "TradeReportingExtractsService" - {
    val ec: ExecutionContext       = scala.concurrent.ExecutionContext.Implicits.global
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockConnector = mock[TradeReportingExtractsConnector]
    val mockMessages  = mock[Messages]
    val httpClient    = mock[HttpClientV2]
    val appConfig     = mock[FrontendAppConfig]

    when(mockMessages("accountsYouHaveAuthorityOverImport.defaultValue")).thenReturn("Default EORI")
    val service = new TradeReportingExtractsService(httpClient)(appConfig, ec, mockConnector)

    "getEoriList" - {
      "should call connector.getEoriList and return the transformed list" in {
        when(mockConnector.getEoriList()).thenReturn(Future.successful(Seq("EORI 1234", "EORI 5678")))

        val result = service.getEoriList()(mockMessages).futureValue

        result(0).text `mustBe` "Default EORI"
        result(1).text `mustBe` "EORI 1234"
        result(2).text `mustBe` "EORI 5678"
      }

      "should handle an empty EORI list and return only the default SelectItem" in {
        when(mockConnector.getEoriList()).thenReturn(Future.successful(Seq()))

        val result = service.getEoriList()(mockMessages).futureValue

        result.head.text `mustBe` "Default EORI"
      }
    }

    "createReportRequest" - {

      "should return a reference when OK" in {

        when(mockConnector.createReportRequest(any())(any())).thenReturn(Future.successful(Seq("Reference")))

        val result = service
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

        result mustBe a[Seq[String]]
        result mustBe Seq("Reference")

      }
    }

    "should return an error when not OK" in {

      when(mockConnector.createReportRequest(any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("error", 400)))

      val result = service
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

      result mustBe a[UpstreamErrorResponse]

    }

    "hasReachedSubmissionLimit" - {

      "return true when connector returns true" in {
        when(mockConnector.hasReachedSubmissionLimit("EORI123")(hc)).thenReturn(Future.successful(true))
        val result = service.hasReachedSubmissionLimit("EORI123").futureValue
        result mustBe true
      }

      "return false when connector returns false" in {
        when(mockConnector.hasReachedSubmissionLimit("EORI123")(hc)).thenReturn(Future.successful(false))
        val result = service.hasReachedSubmissionLimit("EORI123").futureValue
        result mustBe false
      }

      "fail the future if the connector fails" in {
        when(mockConnector.hasReachedSubmissionLimit("EORI123")(hc))
          .thenReturn(Future.failed(new RuntimeException("error")))
        val thrown = intercept[RuntimeException] {
          service.hasReachedSubmissionLimit("EORI123").futureValue
        }
        thrown.getMessage must include("error")
      }
    }

    "getUserDetails" - {

      "should return user details when connector returns them" in {

        val companyInformation = CompanyInformation(
          name = "Test Company",
          consent = "1",
          address = AddressInformation(
            streetAndNumber = "123 Test Street",
            city = "Test City",
            postalCode = Some("12345"),
            countryCode = "GB"
          )
        )
        val eori               = "GB123456789000"
        val userDetails        = UserDetails(
          eori = eori,
          additionalEmails = Seq.empty,
          authorisedUsers = Seq.empty,
          companyInformation = companyInformation,
          notificationEmail = NotificationEmail("test@test.com", LocalDateTime.now())
        )
        when(mockConnector.getUserDetails(eori)).thenReturn(Future.successful(userDetails))
        val result             = service.getUserDetails(eori).futureValue
        result mustBe userDetails
      }

      "should fail when connector throws an exception" in {
        val eori   = "GB123456789000"
        when(mockConnector.getUserDetails(eori)).thenReturn(Future.failed(new RuntimeException("Connector error")))
        val thrown = intercept[RuntimeException] {
          service.getUserDetails(eori).futureValue
        }
        thrown.getMessage must include("Connector error")
      }
    }
  }
}
