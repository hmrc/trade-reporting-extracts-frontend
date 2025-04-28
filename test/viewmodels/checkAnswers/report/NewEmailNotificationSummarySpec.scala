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
import models.{CheckMode, UserAnswers}
import pages.report.NewEmailNotificationPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class NewEmailNotificationSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "NewEmailNotificationSummary.row" - {

    "must return a SummaryListRow when an answer is present" in {
      val answer      = "test@example.com"
      val userAnswers = UserAnswers("id").set(NewEmailNotificationPage, answer).success.value

      val result = NewEmailNotificationSummary.row(userAnswers)

      result mustBe Some(
        SummaryListRow(
          key = "newEmailNotification.checkYourAnswersLabel",
          value = ValueViewModel(Text(HtmlFormat.escape(answer).toString)),
          actions = Some(
            Actions(items =
              Seq(
                ActionItemViewModel(
                  "site.change",
                  controllers.report.routes.NewEmailNotificationController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(messages("newEmailNotification.change.hidden"))
              )
            )
          )
        )
      )
    }

    "must return None when no answer is present" in {
      val userAnswers = UserAnswers("id")

      val result = NewEmailNotificationSummary.row(userAnswers)

      result mustBe None
    }
  }
}
