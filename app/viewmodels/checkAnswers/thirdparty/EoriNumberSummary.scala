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
import pages.thirdparty.EoriNumberPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object EoriNumberSummary {

  def checkYourAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(EoriNumberPage).map { answer =>
      SummaryListRowViewModel(
        key = "eoriNumber.checkYourAnswersLabel",
        value = ValueViewModel(HtmlFormat.escape(answer).toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.thirdparty.routes.EoriNumberController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("eoriNumber.change.hidden"))
        )
      )
    }

  def detailsRow(eori: String)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      SummaryListRowViewModel(
      key = "thirdPartyDetails.eoriNumber.label",
      value = ValueViewModel(HtmlFormat.escape(eori).toString)
      )
    )
}
