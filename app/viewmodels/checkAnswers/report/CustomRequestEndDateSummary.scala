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

import controllers.routes
import jakarta.inject.Inject
import models.{CheckMode, UserAnswers}
import pages.report.CustomRequestEndDatePage
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import utils.ReportHelpers
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class CustomRequestEndDateSummary @Inject() (reportHelpers: ReportHelpers) {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CustomRequestEndDatePage).map { answer =>

      implicit val lang: Lang = messages.lang
      val moreThanOneReport   = reportHelpers.isMoreThanOneReport(answers)
      SummaryListRowViewModel(
        key = if (moreThanOneReport) { "customRequestEndDate.pluralReport.checkYourAnswersLabel" }
        else {
          "customRequestEndDate.singleReport.checkYourAnswersLabel"
        },
        value = ValueViewModel(answer.format(dateTimeFormat())),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.report.routes.CustomRequestEndDateController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(if (moreThanOneReport) {
              messages("customRequestEndDate.pluralReport.change.hidden")
            } else {
              messages("customRequestEndDate.singleReport.change.hidden")
            })
        )
      )
    }
}
