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

package pages.report

import base.SpecBase
import models.UserAnswers
import models.report.ReportDateRange

import java.time.LocalDate

class ReportDateRangePageSpec extends SpecBase {

  "ReportDateRangePage" - {

    "must keep custom date fields when value is Some(CustomDateRange)" in {
      val userAnswers = UserAnswers("id")
        .set(CustomRequestStartDatePage, LocalDate.parse("2025-01-01"))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.parse("2025-12-31"))
        .success
        .value

      val result = ReportDateRangePage.cleanup(Some(ReportDateRange.CustomDateRange), userAnswers).success.value

      result.get(CustomRequestStartDatePage) mustBe Some(LocalDate.parse("2025-01-01"))
      result.get(CustomRequestEndDatePage) mustBe Some(LocalDate.parse("2025-12-31"))
    }

    "must remove CustomRequestStartDatePage and CustomRequestEndDatePage when value is Some(other than CustomDateRange)" in {
      val userAnswers = UserAnswers("id")
        .set(CustomRequestStartDatePage, LocalDate.parse("2025-01-01"))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.parse("2025-12-31"))
        .success
        .value

      val result = ReportDateRangePage.cleanup(Some(ReportDateRange.LastCalendarMonth), userAnswers).success.value

      result.get(CustomRequestStartDatePage) must not be defined
      result.get(CustomRequestEndDatePage)   must not be defined
    }

  }
}
