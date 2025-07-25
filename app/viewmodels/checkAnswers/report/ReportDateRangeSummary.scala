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

import controllers.report.routes
import models.report.ReportDateRange
import models.{CheckMode, UserAnswers}
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, ReportDateRangePage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.{DateTimeFormats, ReportHelpers}
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.{LocalDate, ZoneOffset}

object ReportDateRangeSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ReportDateRangePage).map { answer =>
      val moreThanOneReport = ReportHelpers.isMoreThanOneReport(answers)
      val value             = ValueViewModel(answer match
        case ReportDateRange.CustomDateRange =>
          val startDate = answers
            .get(CustomRequestStartDatePage)
            .map(_.format(dateTimeFormat()(lang = messages.lang)))
            .getOrElse("")
          val endDate   = answers
            .get(CustomRequestEndDatePage)
            .map(_.format(dateTimeFormat()(lang = messages.lang)))
            .getOrElse("")
          HtmlContent(HtmlFormat.escape(startDate + " to " + endDate))
        case _                               =>
          val startEndDate = DateTimeFormats.lastFullCalendarMonth(LocalDate.now(ZoneOffset.UTC))
          HtmlContent(
            HtmlFormat.escape(
              messages(
                s"reportDateRange.lastFullCalendarMonth.checkYourAnswersLabel",
                startEndDate._1.format(dateTimeFormat()(messages.lang)),
                startEndDate._2.format(dateTimeFormat()(messages.lang))
              )
            )
          )
      )

      SummaryListRowViewModel(
        key =
          if (moreThanOneReport) "reportDateRange.pluralReport.checkYourAnswersLabel"
          else {
            "reportDateRange.singleReport.checkYourAnswersLabel"
          },
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ReportDateRangeController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(if (moreThanOneReport) {
              messages("reportDateRange.pluralReport.change.hidden")
            } else {
              messages("reportDateRange.singleReport.change.hidden")
            })
        )
      )
    }
}
