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
import models.{ReportTypeImport, ReportTypeName}
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

}
