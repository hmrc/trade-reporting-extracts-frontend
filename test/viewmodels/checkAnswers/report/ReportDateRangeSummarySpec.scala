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
import pages.report.ReportDateRangePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class ReportDateRangeSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ReportDateRangeSummary.row" - {

    "must return a SummaryListRow when an answer is present" in {
      val answer  = ReportDateRange.Last31Days
      val answers = UserAnswers("id").set(ReportDateRangePage, answer).success.value

      val result = ReportDateRangeSummary.row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "reportDateRange.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(s"reportDateRange.$answer")))),
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
