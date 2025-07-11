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

package pages.report

import base.SpecBase
import models.UserAnswers
import models.report.EmailSelection

class MaybeAdditionalEmailPageSpec extends SpecBase {

  "MaybeAdditionalEmailPage" - {

    "must remove EmailSelectionPage and NewEmailNotificationPage when value is Some(false)" in {
      val userAnswers = UserAnswers("id")
        .set(EmailSelectionPage, Set("email1@test.com"))
        .success
        .value
        .set(NewEmailNotificationPage, "test@example.com")
        .success
        .value
      val result      = MaybeAdditionalEmailPage.cleanup(Some(false), userAnswers).success.value

      result.get(EmailSelectionPage)       must not be defined
      result.get(NewEmailNotificationPage) must not be defined
    }

    "must not remove anything when value is Some(true)" in {
      val userAnswers = UserAnswers("id")
        .set(EmailSelectionPage, Set("email1@test.com"))
        .success
        .value
        .set(NewEmailNotificationPage, "test@example.com")
        .success
        .value

      val result = MaybeAdditionalEmailPage.cleanup(Some(true), userAnswers).success.value

      result.get(EmailSelectionPage) mustBe Some(Set("email1@test.com"))
      result.get(NewEmailNotificationPage) mustBe Some("test@example.com")
    }

  }
}
