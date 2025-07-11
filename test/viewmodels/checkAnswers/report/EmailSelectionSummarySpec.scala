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
import models.UserAnswers
import models.report.EmailSelection
import pages.report.{EmailSelectionPage, MaybeAdditionalEmailPage, NewEmailNotificationPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class EmailSelectionSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "EmailSelectionSummary" - {

    "must return a SummaryListRow when MaybeAdditionalEmailPage is true and EmailSelectionPage has values" in {
      val userAnswers = UserAnswers("id")
        .set(MaybeAdditionalEmailPage, true)
        .success
        .value
        .set(EmailSelectionPage, Set("email1@test.com", "email2@test.com"))
        .success
        .value
      val result      = EmailSelectionSummary.row(userAnswers)

      result mustBe defined
      result.get.key.content.asHtml.body   must include("emailSelection.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include("email1@test.com")
      result.get.value.content.asHtml.body must include("email2@test.com")
    }

    "must return a SummaryListRow with new email when AddNewEmailValue is selected and NewEmailNotificationPage is defined" in {
      val userAnswers = UserAnswers("id")
        .set(MaybeAdditionalEmailPage, true)
        .success
        .value
        .set(EmailSelectionPage, Set(models.report.EmailSelection.AddNewEmailValue))
        .success
        .value
        .set(NewEmailNotificationPage, "new@example.com")
        .success
        .value

      val result = EmailSelectionSummary.row(userAnswers)

      result mustBe defined
      result.get.value.content.asHtml.body must include("new@example.com")
    }

    "must return None when MaybeAdditionalEmailPage is false" in {
      val userAnswers = UserAnswers("id")
        .set(MaybeAdditionalEmailPage, false)
        .success
        .value

      val result = EmailSelectionSummary.row(userAnswers)

      result mustBe None
    }

    "must return None when MaybeAdditionalEmailPage is not defined" in {
      val userAnswers = UserAnswers("id")

      val result = EmailSelectionSummary.row(userAnswers)

      result mustBe None
    }
  }
}
