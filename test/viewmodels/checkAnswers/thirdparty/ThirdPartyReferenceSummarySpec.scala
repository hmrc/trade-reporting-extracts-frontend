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
import models.{CheckMode, UserAnswers}
import pages.thirdparty.ThirdPartyReferencePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class ThirdPartyReferenceSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ThirdPartyReferenceSummarySpec.checkYourAnswersRow" - {

    "must return a SummaryListRow when an answer is present" in {
      val answer      = "thirdPartyRef"
      val userAnswers = UserAnswers("id").set(ThirdPartyReferencePage, answer).success.value

      val result = ThirdPartyReferenceSummary.checkYourAnswersRow(userAnswers)

      result mustBe Some(
        SummaryListRow(
          key = "thirdPartyReference.checkYourAnswersLabel",
          value = ValueViewModel(Text(HtmlFormat.escape(answer).toString)),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.thirdparty.routes.ThirdPartyReferenceController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("thirdPartyReference.change.hidden"))
              )
            )
          )
        )
      )
    }
  }

  "must return None when no answer is present" in {
    val userAnswers = UserAnswers("id")

    val result = ThirdPartyReferenceSummary.checkYourAnswersRow(userAnswers)

    result mustBe None
  }

  ".detailsRow" - {
    "must return summary list row when given a reference" in {
      val result = ThirdPartyReferenceSummary.detailsRow(Some("ref"), false)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyReference.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("ref").toString)
        )
      )
    }

    "must return summary list row with N/A when no reference is given" in {
      val result = ThirdPartyReferenceSummary.detailsRow(None, false)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyReference.checkYourAnswersLabel",
          value = ValueViewModel("site.notApplicable")
        )
      )
    }

    "when tpEnabledAndNotBusinessDetailsRow true, return the summary row with change action" in {
      val result = ThirdPartyReferenceSummary.detailsRow(Some("ref"), true)

      result mustBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyReference.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("ref").toString),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              "#"
            ).withVisuallyHiddenText(messages("thirdPartyReference.change.hidden"))
          )
        )
      )
    }
  }
}
