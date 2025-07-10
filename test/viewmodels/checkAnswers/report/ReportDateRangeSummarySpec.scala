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

package viewmodels.checkAnswers.report

import base.SpecBase
import models.report.ReportDateRange
import models.{CheckMode, UserAnswers}
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, ReportDateRangePage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate

class ReportDateRangeSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ReportDateRangeSummary.row" - {

    "must return a SummaryListRow when last calendar month " in {
      val answer  = ReportDateRange.LastFullCalendarMonth
      val answers = UserAnswers("id").set(ReportDateRangePage, answer).success.value

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "reportDateRange.checkYourAnswersLabel",
          value =
            ValueViewModel(HtmlContent(HtmlFormat.escape(messages(s"reportDateRange.$answer.checkYourAnswersLabel")))),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.ReportDateRangeController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("reportDateRange.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return a SummaryListRow when customDateRange selected" in {
      val answer  = ReportDateRange.CustomDateRange
      val answers = UserAnswers("id")
        .set(ReportDateRangePage, answer)
        .success
        .value
        .set(CustomRequestStartDatePage, LocalDate.of(2025, 1, 1))
        .success
        .value
        .set(CustomRequestEndDatePage, LocalDate.of(2025, 1, 2))
        .success
        .value

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "reportDateRange.checkYourAnswersLabel",
          value =
            ValueViewModel(HtmlContent(HtmlFormat.escape(messages(s"reportDateRange.$answer.checkYourAnswersLabel")))),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.ReportDateRangeController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("reportDateRange.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val answers = UserAnswers("id")

      val result = ReportDateRangeSummary.row(answers)

      result mustBe None
    }
  }
}
