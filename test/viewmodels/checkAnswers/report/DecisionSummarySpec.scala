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
import models.report.Decision
import models.{CheckMode, UserAnswers}
import pages.report.DecisionPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class DecisionSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "DecisionSummary.row" - {

    "must return a SummaryListRow when an answer is present" in {
      val answer  = Decision.values.head
      val answers = UserAnswers("id").set(DecisionPage, answer).get

      val result = DecisionSummary.row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "decision.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(s"decision.$answer")))),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.DecisionController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("decision.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val answers = UserAnswers("id")

      val result = DecisionSummary.row(answers)

      result mustBe None
    }
  }
}
