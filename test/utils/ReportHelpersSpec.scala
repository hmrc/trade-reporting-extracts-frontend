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

package utils

import base.SpecBase
import models.report.ReportTypeImport
import org.scalatest.matchers.must.Matchers
import pages.report.ReportTypeImportPage

class ReportHelpersSpec extends SpecBase with Matchers {

  ".isMoreThanOneReport" - {

    "if more than one report selected, return true" in {

      val ua = emptyUserAnswers
        .set(
          ReportTypeImportPage,
          Set(ReportTypeImport.ImportItem, ReportTypeImport.ImportHeader)
        )
        .success
        .value

      val result = ReportHelpers.isMoreThanOneReport(ua)
      result mustBe true
    }

    "if only one report selected, return false" in {

      val ua = emptyUserAnswers
        .set(
          ReportTypeImportPage,
          Set(ReportTypeImport.ImportItem)
        )
        .success
        .value

      val result = ReportHelpers.isMoreThanOneReport(ua)
      result mustBe false
    }
  }

  ".formatBytes" - {
    "must return bytes when less than 1KB" in {
      ReportHelpers.formatBytes(500L) mustBe "500.00 bytes"
    }

    "must return KB when less than 1MB" in {
      ReportHelpers.formatBytes(1024L * 500) mustBe "500.00 KB"
    }

    "must return MB when less than 1GB" in {
      ReportHelpers.formatBytes(1024L * 1024 * 2) mustBe "2.00 MB"
    }

    "must return GB when less than 1TB" in {
      ReportHelpers.formatBytes(1024L * 1024 * 1024 * 5) mustBe "5.00 GB"
    }

    "must return TB for large values" in {
      ReportHelpers.formatBytes(1024L * 1024 * 1024 * 1024 * 3) mustBe "3.00 TB"
    }

    "must handle 0 bytes" in {
      ReportHelpers.formatBytes(0L) mustBe "0.00 KB"
    }

    "must handle exactly 1KB" in {
      ReportHelpers.formatBytes(1024L) mustBe "1.00 KB"
    }

    "must handle values slightly less than the next unit" in {
      ReportHelpers.formatBytes(1023L) mustBe "1023.00 bytes"
      ReportHelpers.formatBytes(
        1024L * 1024 - 1
      ) mustBe "1024.00 KB" // This will be 1023.999... KB, which rounds to 1024.00 KB due to formatting.
      // Or, depending on exact implementation, it might be just under 1MB.
      // The current implementation will show it as 1024.00 KB.
      // A more precise test might be needed if specific rounding for "just under" is critical.
      ReportHelpers.formatBytes((1024L * 1024 * 1024) - 1) mustBe "1024.00 MB"
    }

    "must handle values slightly more than a unit" in {
      ReportHelpers.formatBytes(1025L) mustBe "1.00 KB" // 1025 / 1024 = 1.0009...
      ReportHelpers.formatBytes(1024L * 1024 + 1) mustBe "1.00 MB"
    }
    "must correctly format to two decimal places" in {
      ReportHelpers.formatBytes(1500L) mustBe "1.46 KB" // 1500 / 1024 = 1.4648...
      ReportHelpers.formatBytes(1024L * 1024 * 1 + 1024 * 512) mustBe "1.50 MB" // 1.5 MB
    }
  }

  ".getReportType" - {

    "must return correct label for known report types" in {
      ReportHelpers.getReportType(models.ReportTypeName.IMPORTS_HEADER_REPORT) mustBe "Import header"
      ReportHelpers.getReportType(models.ReportTypeName.IMPORTS_ITEM_REPORT) mustBe "Import item"
      ReportHelpers.getReportType(models.ReportTypeName.IMPORTS_TAXLINE_REPORT) mustBe "Import tax line"
      ReportHelpers.getReportType(models.ReportTypeName.EXPORTS_ITEM_REPORT) mustBe "Export item"
    }

    "must throw IllegalArgumentException when passed null" in {
      val exception = intercept[IllegalArgumentException] {
        ReportHelpers.getReportType(null)
      }
      exception.getMessage must include("Unknown or null report type")
    }

  }

  ".reportStatusDisplayData" - {

    val reportName = "testReport"
    val reportRef  = "ref123"
    val startDate  = "2024-01-01"
    val endDate    = "2024-02-01"

    "must return correct data for COMPLETE" in {
      val result = ReportHelpers.reportStatusDisplayData(
        models.ReportStatus.COMPLETE,
        reportName,
        reportRef,
        startDate,
        endDate
      )
      result.key mustBe "requestedReports.status.complete"
      result.cssClass mustBe "govuk-tag--green"
      result.maybeRedirect mustBe None
    }

    "must return correct data for ERROR" in {
      val result = ReportHelpers.reportStatusDisplayData(
        models.ReportStatus.ERROR,
        reportName,
        reportRef,
        startDate,
        endDate
      )
      result.key mustBe "requestedReports.status.error"
      result.cssClass mustBe "govuk-tag--red"
      result.maybeRedirect.value must include(
        controllers.problem.routes.ReportFailedController.onPageLoad(reportName, reportRef).url
      )
    }

    "must return correct data for IN_PROGRESS" in {
      val result = ReportHelpers.reportStatusDisplayData(
        models.ReportStatus.IN_PROGRESS,
        reportName,
        reportRef,
        startDate,
        endDate
      )
      result.key mustBe "requestedReports.status.inProgress"
      result.cssClass mustBe "govuk-tag--blue"
      result.maybeRedirect mustBe None
    }

    "must return correct data for NO_DATA_AVAILABLE" in {
      val result = ReportHelpers.reportStatusDisplayData(
        models.ReportStatus.NO_DATA_AVAILABLE,
        reportName,
        reportRef,
        startDate,
        endDate
      )
      result.key mustBe "requestedReports.status.noDataAvailable"
      result.cssClass mustBe "govuk-tag--red"
      result.maybeRedirect.value must include(
        controllers.problem.routes.NoDataFoundController.onPageLoad(reportName, reportRef, startDate, endDate).url
      )
    }
  }

}
