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

class MaybeAdditionalEmailSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "MaybeAdditionalEmailSummary.row" - {

    "must return a SummaryListRow with 'Yes' when the answer is true" in {
      val initialAnswers = UserAnswers("id")

      val updatedAnswers = initialAnswers
        .set(MaybeAdditionalEmailPage, true)
        .flatMap(_.set(EmailSelectionPage, Set("email1@test.com", "email3@test.com")))
        .flatMap(_.set(NewEmailNotificationPage, "test@gmail.com"))

      val result = MaybeAdditionalEmailSummary.row(updatedAnswers.success.value)

      result mustBe defined
      result.get.value.content.toString must include(messages("site.yes"))
    }

    "must return a SummaryListRow with 'No' when the answer is false" in {
      val initialAnswers = UserAnswers("id")

      val updatedAnswers = initialAnswers
        .set(MaybeAdditionalEmailPage, false)
        .flatMap(_.set(EmailSelectionPage, Set("email2@test.com")))
        .flatMap(_.set(NewEmailNotificationPage, "another@example.com"))

      val result = MaybeAdditionalEmailSummary.row(updatedAnswers.success.value)

      result mustBe defined
      result.get.value.content.toString must include(messages("site.no"))
    }
  }
}
