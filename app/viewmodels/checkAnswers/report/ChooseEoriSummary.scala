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
import models.report.ChooseEori
import models.{CheckMode, UserAnswers}
import pages.report.{AccountsYouHaveAuthorityOverImportPage, ChooseEoriPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object ChooseEoriSummary {

  def row(answers: UserAnswers, eori: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ChooseEoriPage).map { answer =>

      val value = ValueViewModel(
        answer match
          case ChooseEori.Myeori      =>
            HtmlContent(HtmlFormat.escape(messages(s"chooseEori.$answer", eori)))
          case ChooseEori.Myauthority =>
            answers
              .get(AccountsYouHaveAuthorityOverImportPage)
              .map { accounts =>
                HtmlContent(
                  HtmlFormat.escape(accounts)
                )
              }
              .getOrElse("")
      )

      SummaryListRowViewModel(
        key = "chooseEori.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", routes.ChooseEoriController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("chooseEori.change.hidden"))
        )
      )
    }
}
