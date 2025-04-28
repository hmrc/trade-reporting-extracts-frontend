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
import models.ReportTypeImport
import org.scalatest.matchers.must.Matchers
import pages.report.ReportTypeImportPage

class ReportHelpersSpec extends SpecBase with Matchers {

  ".isMoreThanOneReport" - {

    val reportHelpers = new ReportHelpers

    "if more than one report selected, return true" in {

      val ua = emptyUserAnswers
        .set(
          ReportTypeImportPage,
          Set(ReportTypeImport.ImportItem, ReportTypeImport.ImportHeader)
        )
        .success
        .value

      val result = reportHelpers.isMoreThanOneReport(ua)
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

      val result = reportHelpers.isMoreThanOneReport(ua)
      result mustBe false
    }
  }
}
