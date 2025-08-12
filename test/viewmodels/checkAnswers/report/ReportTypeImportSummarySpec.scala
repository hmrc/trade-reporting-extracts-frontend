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
import models.report.ReportTypeImport
import models.{CheckMode, UserAnswers}
import pages.report.ReportTypeImportPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class ReportTypeImportSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ReportTypeImportSummary.row" - {

    "must return a SummaryListRow when an answer is present (single report)" in {
      val answers = UserAnswers("id").set(ReportTypeImportPage, Set(ReportTypeImport.ImportHeader)).get

      val expectedValue = ValueViewModel(
        HtmlContent(
          Set(ReportTypeImport.ImportHeader)
            .map(rt => HtmlFormat.escape(messages(s"reportTypeImport.$rt")).toString)
            .mkString(",<br>")
        )
      )

      val result = ReportTypeImportSummary.row(answers)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "reportTypeImport.singleReport.checkYourAnswersLabel",
          value = expectedValue,
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.report.routes.ReportTypeImportController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("reportTypeImport.singleReport.change.hidden"))
          )
        )
      )
    }

    "must return a SummaryListRow when an answer is present (plural report)" in {
      val answers = UserAnswers("id")
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportHeader, ReportTypeImport.ImportItem))
        .get

      val expectedValue = ValueViewModel(
        HtmlContent(
          Set(ReportTypeImport.ImportHeader, ReportTypeImport.ImportItem)
            .map(rt => HtmlFormat.escape(messages(s"reportTypeImport.$rt")).toString)
            .mkString(",<br>")
        )
      )

      val result = ReportTypeImportSummary.row(answers)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "reportTypeImport.pluralReport.checkYourAnswersLabel",
          value = expectedValue,
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.report.routes.ReportTypeImportController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("reportTypeImport.pluralReport.change.hidden"))
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val answers = UserAnswers("id")
      val result  = ReportTypeImportSummary.row(answers)
      result mustBe None
    }
  }
}
