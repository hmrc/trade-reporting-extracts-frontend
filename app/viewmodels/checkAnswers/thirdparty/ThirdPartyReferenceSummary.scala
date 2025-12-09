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

import models.{CheckMode, UserAnswers}
import pages.thirdparty.ThirdPartyReferencePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ThirdPartyReferenceSummary {

  def checkYourAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ThirdPartyReferencePage).map { answer =>
      SummaryListRowViewModel(
        key = "thirdPartyReference.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(answer).toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.thirdparty.routes.ThirdPartyReferenceController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("thirdPartyReference.change.hidden"))
        )
      )
    }

  def detailsRow(reference: Option[String], isThirdPartyEnabled: Boolean, thirdPartyEori: String)(implicit
    messages: Messages
  ): Option[SummaryListRow] = {
    val value = reference match {
      case Some(value) => ValueViewModel(HtmlFormat.escape(reference.get).toString)
      case None        => ValueViewModel("site.notApplicable")
    }
    if (isThirdPartyEnabled && thirdPartyEori.nonEmpty) {
      Some(
        SummaryListRowViewModel(
          key = "thirdPartyReference.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.editThirdParty.routes.EditThirdPartyReferenceController.onPageLoad(thirdPartyEori).url
            )
              .withVisuallyHiddenText(messages("thirdPartyReference.change.hidden"))
          )
        )
      )
    } else {
      Some(
        SummaryListRowViewModel(
          key = "thirdPartyReference.checkYourAnswersLabel",
          value = value
        )
      )
    }
  }

}
