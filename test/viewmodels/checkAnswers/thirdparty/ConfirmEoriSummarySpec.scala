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
import models.thirdparty.ConfirmEori
import models.{CheckMode, UserAnswers}
import pages.thirdparty.ConfirmEoriPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class ConfirmEoriSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ConfirmEoriSummary" - {

    "must return a summary list row when answer is present" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(ConfirmEoriPage, ConfirmEori.Yes)
        .success
        .value

      val result: Option[SummaryListRow] = ConfirmEoriSummary.row(userAnswers)

      result mustBe defined
      result.get.key.content.asHtml.toString   must include("confirmEori.checkYourAnswersLabel")
      result.get.value.content.asHtml.toString must include(messages("confirmEori.yes"))
      result.get.actions.head.items.head.href mustEqual controllers.thirdparty.routes.ConfirmEoriController
        .onPageLoad(CheckMode)
        .url
    }

    "must return None when answer is not present" in {
      val userAnswers = UserAnswers(userAnswersId)

      val result = ConfirmEoriSummary.row(userAnswers)

      result mustBe None
    }
  }
}
