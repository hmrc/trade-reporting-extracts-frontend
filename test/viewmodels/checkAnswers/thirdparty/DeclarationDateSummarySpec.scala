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

package viewmodels.checkAnswers.thirdparty

import base.SpecBase
import models.thirdparty.DeclarationDate
import models.{CheckMode, UserAnswers}
import pages.thirdparty.DeclarationDatePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class DeclarationDateSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "DeclarationDateSummary.row" - {

    "must return a SummaryListRow when an answer is present" in {
      val answer  = DeclarationDate.values.head
      val answers = UserAnswers("id").set(DeclarationDatePage, answer).get

      val result = DeclarationDateSummary.row(answers)

      result mustBe Some(
        SummaryListRow(
          key = "declarationDate.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(s"declarationDate.$answer")))),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.thirdparty.routes.DeclarationDateController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("declarationDate.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val answers = UserAnswers("id")

      val result = DeclarationDateSummary.row(answers)

      result mustBe None
    }
  }
}
