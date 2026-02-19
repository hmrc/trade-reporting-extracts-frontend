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
import models.EoriRole
import models.report.{ChooseEori, Decision, EmailSelection, ReportDateRange, ReportTypeImport}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.report.{ChooseEoriPage, CustomRequestEndDatePage, CustomRequestStartDatePage, DecisionPage, EmailSelectionPage, EoriRolePage, MaybeAdditionalEmailPage, NewEmailNotificationPage, ReportDateRangePage, ReportNamePage, ReportTypeImportPage, SelectThirdPartyEoriPage}

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class ReportRequestDataServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with Matchers {
  val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  "buildReportRequest" - {

    "should build report request correctly" in {

      val fixedInstant = Instant.parse("2025-05-05T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val reportRequestDataService = new ReportRequestDataService(fixedClock)

      val userAnswers = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .get
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .get
        .set(EoriRolePage, Set(EoriRole.Importer))
        .get
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .get
        .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
        .get
        .set(ReportNamePage, "MyReport")
        .get
        .set(MaybeAdditionalEmailPage, true)
        .get
        .set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue))
        .get
        .set(NewEmailNotificationPage, "example@email.com")
        .get

      val result = reportRequestDataService.buildReportRequest(userAnswers, "eori").get

      result.eori mustBe "eori"
      result.dataType mustBe "import"
      result.whichEori mustBe "eori"
      result.eoriRole mustBe Set("importer")
      result.reportType mustBe Set("importItem")
      result.reportStartDate mustBe "2025-04-01"
      result.reportEndDate mustBe "2025-04-30"
      result.reportName mustBe "MyReport"
      result.additionalEmail mustBe Some(Set("example@email.com"))

    }

    "should get correct report dates when last calendar month" in {
      val fixedInstant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val reportRequestDataService = new ReportRequestDataService(fixedClock)

      val userAnswers = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .get
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .get
        .set(EoriRolePage, Set(EoriRole.Importer))
        .get
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .get
        .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
        .get
        .set(ReportNamePage, "MyReport")
        .get
        .set(MaybeAdditionalEmailPage, true)
        .get
        .set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue))
        .get
        .set(NewEmailNotificationPage, "example@email.com")
        .get

      val result = reportRequestDataService.buildReportRequest(userAnswers, "eori").get

      result.eori mustBe "eori"
      result.dataType mustBe "import"
      result.whichEori mustBe "eori"
      result.eoriRole mustBe Set("importer")
      result.reportType mustBe Set("importItem")
      result.reportStartDate mustBe "2025-04-01"
      result.reportEndDate mustBe "2025-04-30"
      result.reportName mustBe "MyReport"
      result.additionalEmail mustBe Some(Set("example@email.com"))

    }

    "should get correct report dates when user customises their range" in {
      val fixedInstant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val reportRequestDataService = new ReportRequestDataService(fixedClock)

      val userAnswers = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .get
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .get
        .set(EoriRolePage, Set(EoriRole.Importer))
        .get
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .get
        .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
        .get
        .set(CustomRequestStartDatePage, LocalDate.of(2022, 1, 1))
        .get
        .set(CustomRequestEndDatePage, LocalDate.of(2022, 2, 1))
        .get
        .set(ReportNamePage, "MyReport")
        .get
        .set(MaybeAdditionalEmailPage, true)
        .get
        .set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue))
        .get
        .set(NewEmailNotificationPage, "example@email.com")
        .get

      val result = reportRequestDataService.buildReportRequest(userAnswers, "eori").get

      result.eori mustBe "eori"
      result.dataType mustBe "import"
      result.whichEori mustBe "eori"
      result.eoriRole mustBe Set("importer")
      result.reportType mustBe Set("importItem")
      result.reportStartDate mustBe "2022-01-01"
      result.reportEndDate mustBe "2022-02-01"
      result.reportName mustBe "MyReport"
      result.additionalEmail mustBe Some(Set("example@email.com"))

    }

    "if report date range page not answered and third party selected, get dates from custom request start and end date page" in {
      val fixedInstant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val reportRequestDataService = new ReportRequestDataService(fixedClock)

      val userAnswers = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .get
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .get
        .set(SelectThirdPartyEoriPage, "traderEori")
        .get
        .set(EoriRolePage, Set(EoriRole.Importer))
        .get
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .get
        .set(CustomRequestStartDatePage, LocalDate.of(2022, 1, 1))
        .get
        .set(CustomRequestEndDatePage, LocalDate.of(2022, 2, 1))
        .get
        .set(ReportNamePage, "MyReport")
        .get
        .set(MaybeAdditionalEmailPage, true)
        .get
        .set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue))
        .get
        .set(NewEmailNotificationPage, "example@email.com")
        .get

      val result = reportRequestDataService.buildReportRequest(userAnswers, "eori").get

      result.eori mustBe "eori"
      result.dataType mustBe "import"
      result.whichEori mustBe "traderEori"
      result.eoriRole mustBe Set("importer")
      result.reportType mustBe Set("importItem")
      result.reportStartDate mustBe "2022-01-01"
      result.reportEndDate mustBe "2022-02-01"
      result.reportName mustBe "MyReport"
      result.additionalEmail mustBe Some(Set("example@email.com"))
    }

    "should get all additional emails" in {
      val fixedInstant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val reportRequestDataService = new ReportRequestDataService(fixedClock)

      val userAnswers = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .get
        .set(ChooseEoriPage, ChooseEori.Myeori)
        .get
        .set(EoriRolePage, Set(EoriRole.Importer))
        .get
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .get
        .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
        .get
        .set(ReportNamePage, "MyReport")
        .get
        .set(MaybeAdditionalEmailPage, true)
        .get
        .set(EmailSelectionPage, Set(EmailSelection.AddNewEmailValue, "email3@email.com"))
        .get
        .set(NewEmailNotificationPage, "example@email.com")
        .get

      val result = reportRequestDataService.buildReportRequest(userAnswers, "eori").get

      result.eori mustBe "eori"
      result.dataType mustBe "import"
      result.whichEori mustBe "eori"
      result.eoriRole mustBe Set("importer")
      result.reportType mustBe Set("importItem")
      result.reportStartDate mustBe "2025-04-01"
      result.reportEndDate mustBe "2025-04-30"
      result.reportName mustBe "MyReport"
      result.additionalEmail mustBe Some(Set("email3@email.com", "example@email.com"))

    }

    "should set EORI role based on data type if user is third party" in {
      val fixedInstant = Instant.parse("2025-05-20T00:00:00Z")
      val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

      val reportRequestDataService = new ReportRequestDataService(fixedClock)
      when(appConfig.thirdPartyEnabled).thenReturn(true)
      val userAnswers              = emptyUserAnswers
        .set(DecisionPage, Decision.Import)
        .get
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .get
        .set(SelectThirdPartyEoriPage, "thirdPartyEori")
        .get
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
        .get
        .set(CustomRequestStartDatePage, LocalDate.of(2022, 1, 1))
        .get
        .set(CustomRequestEndDatePage, LocalDate.of(2022, 2, 1))
        .get
        .set(ReportNamePage, "MyReport")
        .get
        .set(MaybeAdditionalEmailPage, true)
        .get
        .set(EmailSelectionPage, Set("example2@email.com", EmailSelection.AddNewEmailValue))
        .get
        .set(NewEmailNotificationPage, "example@email.com")
        .get

      val result = reportRequestDataService.buildReportRequest(userAnswers, "eori").get

      result.eori mustBe "eori"
      result.dataType mustBe "import"
      result.whichEori mustBe "thirdPartyEori"
      result.eoriRole mustBe Set("importer")
      result.reportType mustBe Set("importItem")
      result.reportStartDate mustBe "2022-01-01"
      result.reportEndDate mustBe "2022-02-01"
      result.reportName mustBe "MyReport"
      result.additionalEmail mustBe Some(Set("example2@email.com", "example@email.com"))

    }

  }

}
