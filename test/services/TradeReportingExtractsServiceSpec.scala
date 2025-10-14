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
import connectors.TradeReportingExtractsConnector
import models.AccessType.IMPORTS
import models.ConsentStatus.Granted
import models.{AuditDownloadRequest, AuthorisedUser, CompanyInformation, ConsentStatus, NotificationEmail, ThirdPartyDetails, UserActiveStatus, UserDetails}
import models.report.{ReportConfirmation, ReportRequestUserAnswersModel}
import models.thirdparty.{AccountAuthorityOverViewModel, AuthorisedThirdPartiesViewModel, ThirdPartyRequest}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.{Clock, Instant, LocalDate, LocalDateTime, ZoneOffset}
import scala.concurrent.{ExecutionContext, Future}

class TradeReportingExtractsServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with Matchers {

  implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrier()

  "TradeReportingExtractsService" - {
    val ec: ExecutionContext       = scala.concurrent.ExecutionContext.Implicits.global
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockConnector = mock[TradeReportingExtractsConnector]
    val mockMessages  = mock[Messages]

    val clock = Clock.fixed(Instant.parse("2025-10-09T00:00:00Z"), ZoneOffset.UTC)
    val today = LocalDate.now(clock).atStartOfDay()

    when(mockMessages("SelectThirdPartyEori.defaultValue")).thenReturn("Default EORI")
    val service = new TradeReportingExtractsService(clock)(ec, mockConnector)

    "createReportRequest" - {

      "should return a reference when OK" in {

        when(mockConnector.createReportRequest(any())(any()))
          .thenReturn(Future.successful(Seq(ReportConfirmation("MyReport", "importHeader", "Reference"))))

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

        result mustBe a[Seq[ReportConfirmation]]
        result mustBe Seq(ReportConfirmation("MyReport", "importHeader", "Reference"))

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
          consent = Granted
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

    "auditReportDownload" - {

      val reportReference = "some-reference"
      val fileName        = "report.csv"
      val fileUrl         = "http://localhost/report.csv"
      val auditRequest    = AuditDownloadRequest(reportReference, fileName, fileUrl)

      "must call the connector and return true when the connector returns true" in {
        when(mockConnector.auditReportDownload(auditRequest)(hc)).thenReturn(Future.successful(true))

        val result = service.auditReportDownload(reportReference, fileName, fileUrl).futureValue

        result mustBe true
        verify(mockConnector).auditReportDownload(auditRequest)(hc)
      }

      "must call the connector and return false when the connector returns false" in {
        when(mockConnector.auditReportDownload(auditRequest)(hc)).thenReturn(Future.successful(false))

        val result = service.auditReportDownload(reportReference, fileName, fileUrl).futureValue

        result mustBe false
      }

      "must fail the future if the connector fails" in {
        val exception = new RuntimeException("Connector error.")
        when(mockConnector.auditReportDownload(auditRequest)(hc)).thenReturn(Future.failed(exception))

        val thrown = intercept[RuntimeException] {
          service.auditReportDownload(reportReference, fileName, fileUrl).futureValue
        }
        thrown.getMessage must include("Connector error.")
      }
    }

    "getReportRequestLimitNumber" - {
      "should return the report request limit number from the connector" in {
        when(mockConnector.getReportRequestLimitNumber(any()))
          .thenReturn(Future.successful("25"))

        val result = service.getReportRequestLimitNumber.futureValue
        result mustBe "25"
      }

      "should fail the future if the connector fails" in {
        when(mockConnector.getReportRequestLimitNumber(any()))
          .thenReturn(Future.failed(new RuntimeException("error")))

        val thrown = intercept[RuntimeException] {
          service.getReportRequestLimitNumber.futureValue
        }
        thrown.getMessage must include("error")
      }
    }

    "createThirdPartyAddRequest" - {

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

      val confirmation = models.thirdparty.ThirdPartyAddedConfirmation(
        thirdPartyEori = "GB987654321000"
      )

      "should return confirmation when connector succeeds" in {
        when(mockConnector.createThirdPartyAddRequest(thirdPartyRequest))
          .thenReturn(Future.successful(confirmation))

        val result = service.createThirdPartyAddRequest(thirdPartyRequest).futureValue
        result mustBe confirmation
      }

      "should fail when connector fails" in {
        when(mockConnector.createThirdPartyAddRequest(thirdPartyRequest))
          .thenReturn(Future.failed(new RuntimeException("error")))

        val thrown = intercept[RuntimeException] {
          service.createThirdPartyAddRequest(thirdPartyRequest).futureValue
        }
        thrown.getMessage must include("error")
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

      val eori           = "123"
      val thirdPartyEori = "456"

      "should return third party details when connector returns them" in {

        when(mockConnector.getThirdPartyDetails(any(), any())(any()))
          .thenReturn(Future.successful(validThirdPartyDetails))
        val result = service.getThirdPartyDetails(eori, thirdPartyEori).futureValue
        result mustBe validThirdPartyDetails
      }

      "should fail the future if the connector fails" in {

        when(mockConnector.getThirdPartyDetails(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("error")))
        val thrown = intercept[RuntimeException] {
          service.getThirdPartyDetails(eori, thirdPartyEori).futureValue
        }
        thrown.getMessage must include("error")
      }
    }

    "getAuthorisedThirdParties" - {
      val cutoffDate = today.minusDays(3)

      "return third parties with business info when consent is granted" in {

        val accessStart     = today.minusDays(1).toInstant(ZoneOffset.UTC)
        val accessEnd       = today.plusDays(5).toInstant(ZoneOffset.UTC)
        val reportDataStart = cutoffDate.toInstant(ZoneOffset.UTC)

        val eori           = "EORITEST1"
        val authorisedUser = AuthorisedUser(
          "EORITAUTHEST1",
          accessStart,
          Some(accessEnd),
          Some(reportDataStart),
          Some(accessEnd),
          Set(IMPORTS),
          referenceName = Some("aaaaa")
        )
        val userDetails    = UserDetails(
          eori = eori,
          additionalEmails = Seq.empty,
          authorisedUsers = Seq(authorisedUser),
          companyInformation = CompanyInformation("Company", ConsentStatus.Granted),
          notificationEmail = null
        )
        val companyInfo    = CompanyInformation("ThirdParty Ltd", ConsentStatus.Granted)

        when(mockConnector.getUserDetails(eori)).thenReturn(Future.successful(userDetails))
        when(mockConnector.getCompanyInformation("EORITAUTHEST1")).thenReturn(Future.successful(companyInfo))

        val result = service.getAuthorisedThirdParties(eori).futureValue

        result mustBe Seq(
          AuthorisedThirdPartiesViewModel(
            eori = "EORITAUTHEST1",
            businessInfo = Some("ThirdParty Ltd"),
            referenceName = Some("aaaaa"),
            status = UserActiveStatus.Active
          )
        )
      }

      "return third parties with no business info when consent is not granted & no ref when ref is none" in {
        val accessStart     = today.plusDays(2).toInstant(ZoneOffset.UTC)
        val accessEnd       = today.plusDays(10).toInstant(ZoneOffset.UTC)
        val reportDataStart = None

        val eori           = "EORI123"
        val authorisedUser = AuthorisedUser(
          "EORITAUTHEST2",
          accessStart,
          Some(accessEnd),
          reportDataStart,
          Some(accessEnd),
          Set(IMPORTS),
          None
        )
        val userDetails    = UserDetails(
          eori = eori,
          additionalEmails = Seq.empty,
          authorisedUsers = Seq(authorisedUser),
          companyInformation = CompanyInformation("Company", ConsentStatus.Granted),
          notificationEmail = null
        )
        val companyInfo    = CompanyInformation("NoConsent Ltd", ConsentStatus.Denied)

        when(mockConnector.getUserDetails(eori)).thenReturn(Future.successful(userDetails))
        when(mockConnector.getCompanyInformation("EORITAUTHEST2")).thenReturn(Future.successful(companyInfo))

        val result = service.getAuthorisedThirdParties(eori).futureValue

        result mustBe Seq(
          AuthorisedThirdPartiesViewModel(
            eori = "EORITAUTHEST2",
            businessInfo = None,
            referenceName = None,
            status = UserActiveStatus.Upcoming
          )
        )
      }

      "return empty sequence when there are no authorised users" in {
        val eori        = "EORI123"
        val userDetails = UserDetails(
          eori = eori,
          additionalEmails = Seq.empty,
          authorisedUsers = Seq.empty,
          companyInformation = CompanyInformation("Company", ConsentStatus.Granted),
          notificationEmail = null
        )

        when(mockConnector.getUserDetails(eori)).thenReturn(Future.successful(userDetails))

        val result = service.getAuthorisedThirdParties(eori).futureValue

        result mustBe Seq.empty
      }

      "fail the future if getUserDetails fails" in {
        val eori = "EORI123"
        when(mockConnector.getUserDetails(eori)).thenReturn(Future.failed(new RuntimeException("error")))

        val thrown = intercept[RuntimeException] {
          service.getAuthorisedThirdParties(eori).futureValue
        }

        thrown.getMessage must include("error")
      }

      "fail the future if getCompanyInformation fails for any user" in {
        val eori           = "EORI123"
        val authorisedUser = AuthorisedUser(
          "EORITAUTHEST3",
          Instant.now,
          Some(Instant.now),
          Some(Instant.now),
          Some(Instant.now),
          Set(IMPORTS),
          referenceName = Some("aaaaa")
        )
        val userDetails    = UserDetails(
          eori = eori,
          additionalEmails = Seq.empty,
          authorisedUsers = Seq(authorisedUser),
          companyInformation = CompanyInformation("Company", ConsentStatus.Granted),
          notificationEmail = null
        )

        when(mockConnector.getUserDetails(eori)).thenReturn(Future.successful(userDetails))
        when(mockConnector.getCompanyInformation("EORITAUTHEST3"))
          .thenReturn(Future.failed(new RuntimeException("company error")))

        val thrown = intercept[RuntimeException] {
          service.getAuthorisedThirdParties(eori).futureValue
        }

        thrown.getMessage must include("company error")
      }
    }

    "removeThirdParty" - {
      "should return Done when connector succeeds" in {
        val eori           = "EORI123"
        val thirdPartyEori = "EORI456"

        when(mockConnector.removeThirdParty(any(), any())(any()))
          .thenReturn(Future.successful(Done))

        val result = service.removeThirdParty(eori, thirdPartyEori).futureValue

        result mustBe Done
        verify(mockConnector).removeThirdParty(any(), any())(any())
      }

      "should fail when connector fails" in {
        val eori           = "EORI123"
        val thirdPartyEori = "EORI456"

        when(mockConnector.removeThirdParty(any, any)(any()))
          .thenReturn(Future.failed(new RuntimeException("error")))

        val thrown = intercept[RuntimeException] {
          service.removeThirdParty(eori, thirdPartyEori).futureValue
        }
        thrown.getMessage must include("error")
      }
    }

    "getAccountsAuthorityOver" - {
      val eori = "GB123456789000"

      "should return accounts from connector" in {
        val accounts = Seq(
          AccountAuthorityOverViewModel("GB111", Some("Business One"), Some(UserActiveStatus.Active)),
          AccountAuthorityOverViewModel("GB222", None, Some(UserActiveStatus.Upcoming))
        )

        when(mockConnector.getAccountsAuthorityOver(eori))
          .thenReturn(Future.successful(accounts))

        val result = service.getAccountsAuthorityOver(eori).futureValue
        result mustBe accounts
      }

      "should fail when connector fails" in {
        when(mockConnector.getAccountsAuthorityOver(eori))
          .thenReturn(Future.failed(new RuntimeException("connector error")))

        val thrown = intercept[RuntimeException] {
          service.getAccountsAuthorityOver(eori).futureValue
        }
        thrown.getMessage must include("connector error")
      }
    }

    "getSelectThirdPartyEori" - {
      val eori = "GB123456789000"

      "should transform connector response into SelectThirdPartyEori" in {
        val accounts = Seq(
          AccountAuthorityOverViewModel("GB111", Some("Business One"), None),
          AccountAuthorityOverViewModel("GB222", None, None)
        )

        when(mockConnector.getSelectThirdPartyEori(eori))
          .thenReturn(Future.successful(accounts))

        val result = service.getSelectThirdPartyEori(eori).futureValue

        result.values mustBe Seq("GB111 - Business One", "GB222")
      }

      "should return empty SelectThirdPartyEori when connector returns empty" in {
        when(mockConnector.getSelectThirdPartyEori(eori))
          .thenReturn(Future.successful(Seq.empty))

        val result = service.getSelectThirdPartyEori(eori).futureValue

        result.values mustBe Seq.empty
      }

      "should fail when connector fails" in {
        when(mockConnector.getSelectThirdPartyEori(eori))
          .thenReturn(Future.failed(new RuntimeException("error")))

        val thrown = intercept[RuntimeException] {
          service.getSelectThirdPartyEori(eori).futureValue
        }
        thrown.getMessage must include("error")
      }
    }

  }
}
