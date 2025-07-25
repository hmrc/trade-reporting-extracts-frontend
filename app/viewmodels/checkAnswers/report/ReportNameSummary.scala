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
import models.{CheckMode, UserAnswers}
import pages.report.ReportNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.ReportHelpers
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ReportNameSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ReportNamePage).map { answer =>
      val moreThanOneReport = ReportHelpers.isMoreThanOneReport(answers)
      SummaryListRowViewModel(
        key = if (moreThanOneReport) { "reportName.pluralReport.checkYourAnswersLabel" }
        else { "reportName.checkYourAnswersLabel" },
        value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(answer))
          )
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.ReportNameController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(if (moreThanOneReport) { messages("reportName.change.hidden.plural") }
            else { messages("reportName.change.hidden") })
        )
      )

    }
}
