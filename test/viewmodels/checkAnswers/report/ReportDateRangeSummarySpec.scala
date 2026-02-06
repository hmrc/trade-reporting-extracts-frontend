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
import models.report.{ChooseEori, ReportDateRange, ReportTypeImport}
import models.{CheckMode, UserAnswers}
import pages.report.{ChooseEoriPage, CustomRequestEndDatePage, CustomRequestStartDatePage, ReportDateRangePage, ReportTypeImportPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate

class ReportDateRangeSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ReportDateRangeSummary.row" - {

    "must return a SummaryListRow for LastFullCalendarMonth (single report)" in {
      val answer  = ReportDateRange.LastFullCalendarMonth
      val answers = UserAnswers("id").set(ReportDateRangePage, answer).success.value

      val startEndDate  = DateTimeFormats.lastFullCalendarMonth(LocalDate.now())
      val expectedValue = HtmlContent(
        HtmlFormat.escape(
          messages(
            s"reportDateRange.lastFullCalendarMonth.checkYourAnswersLabel",
            startEndDate._1.format(DateTimeFormats.dateTimeFormat()(messages.lang)),
            startEndDate._2.format(DateTimeFormats.dateTimeFormat()(messages.lang))
          )
        )
      )

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "reportDateRange.singleReport.checkYourAnswersLabel",
          value = ValueViewModel(expectedValue),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.report.routes.ReportDateRangeController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("reportDateRange.singleReport.change.hidden"))
          )
        )
      )
    }

    "must return a SummaryListRow for LastFullCalendarMonth (plural report)" in {
      val answer  = ReportDateRange.LastFullCalendarMonth
      val answers = UserAnswers("id")
        .set(ReportDateRangePage, answer)
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem, ReportTypeImport.ExportItem))
        .success
        .value

      val startEndDate  = DateTimeFormats.lastFullCalendarMonth(LocalDate.now())
      val expectedValue = HtmlContent(
        HtmlFormat.escape(
          messages(
            s"reportDateRange.lastFullCalendarMonth.checkYourAnswersLabel",
            startEndDate._1.format(DateTimeFormats.dateTimeFormat()(messages.lang)),
            startEndDate._2.format(DateTimeFormats.dateTimeFormat()(messages.lang))
          )
        )
      )

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "reportDateRange.pluralReport.checkYourAnswersLabel",
          value = ValueViewModel(expectedValue),
          actions = Some(
            uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Actions(
              items = Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.ReportDateRangeController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("reportDateRange.pluralReport.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return a SummaryListRow for CustomDateRange, change link must be Report Date Range when user not third party scenario " in {
      val answer    = ReportDateRange.CustomDateRange
      val startDate = LocalDate.of(2025, 1, 1)
      val endDate   = LocalDate.of(2025, 1, 2)
      val answers   = UserAnswers("id")
        .set(ReportDateRangePage, answer)
        .success
        .value
        .set(CustomRequestStartDatePage, startDate)
        .success
        .value
        .set(CustomRequestEndDatePage, endDate)
        .success
        .value

      val expectedValue = HtmlContent(HtmlFormat.escape("reportDateRange.checkYourAnswersLabel.to"))

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "reportDateRange.singleReport.checkYourAnswersLabel",
          value = ValueViewModel(expectedValue),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.report.routes.ReportDateRangeController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("reportDateRange.singleReport.change.hidden"))
          )
        )
      )
    }

    "must return a SummaryListRow for CustomDateRange, change link must be Report Start when third party scenario" in {
      val answer    = ReportDateRange.CustomDateRange
      val startDate = LocalDate.of(2025, 1, 1)
      val endDate   = LocalDate.of(2025, 1, 2)
      val answers   = UserAnswers("id")
        .set(ChooseEoriPage, ChooseEori.Myauthority)
        .success
        .value
        .set(ReportDateRangePage, answer)
        .success
        .value
        .set(CustomRequestStartDatePage, startDate)
        .success
        .value
        .set(CustomRequestEndDatePage, endDate)
        .success
        .value

      val expectedValue = HtmlContent(HtmlFormat.escape("reportDateRange.checkYourAnswersLabel.to"))

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "reportDateRange.singleReport.checkYourAnswersLabel",
          value = ValueViewModel(expectedValue),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.report.routes.CustomRequestStartDateController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("reportDateRange.singleReport.change.hidden"))
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val answers = UserAnswers("id")
      val result  = ReportDateRangeSummary.row(answers)
      result mustBe None
    }
  }
}
