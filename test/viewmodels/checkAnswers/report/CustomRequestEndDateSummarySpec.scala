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

import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import pages.report.{CustomRequestEndDatePage, ReportTypeImportPage}
import base.SpecBase
import models.report.ReportTypeImport
import models.{CheckMode, UserAnswers}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate
import scala.xml.Text

class CustomRequestEndDateSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  val validAnswer: LocalDate = LocalDate.now().minusDays(10)

  "row" - {
    "when value is entered return the summary row" in {

      val answers = UserAnswers("id").set(CustomRequestEndDatePage, validAnswer).success.value

      val result = CustomRequestEndDateSummary().row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "customRequestEndDate.singleReport.checkYourAnswersLabel",
          value = ValueViewModel(Text(validAnswer.format(dateTimeFormat()(messages.lang))).toString()),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.CustomRequestEndDateController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("customRequestEndDate.singleReport.change.hidden"))
              )
            )
          )
        )
      )
    }

    "when more than one report type selected, return correct view" in {

      val answers = UserAnswers("id")
        .set(ReportTypeImportPage, Set(ReportTypeImport.ImportHeader, ReportTypeImport.ImportItem))
        .success
        .value
        .set(CustomRequestEndDatePage, validAnswer)
        .success
        .value

      val result = CustomRequestEndDateSummary().row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "customRequestEndDate.pluralReport.checkYourAnswersLabel",
          value = ValueViewModel(Text(validAnswer.format(dateTimeFormat()(messages.lang))).toString()),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.CustomRequestEndDateController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("customRequestEndDate.pluralReport.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val answers = UserAnswers("id")

      val result = CustomRequestEndDateSummary().row(answers)

      result mustBe None
    }
  }
}
