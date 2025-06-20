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
import models.report.EmailSelection
import models.report.EmailSelection.{Email1, Email2, Email3}

class EmailSelectionPageSpec extends SpecBase {

  "EmailSelectionPage.cleanup" - {

    "must remove NewEmailNotificationPage when Email3 is not selected" in {
      val userAnswers = emptyUserAnswers
        .set(NewEmailNotificationPage, "new@email.com")
        .success
        .value

      val result = EmailSelectionPage.cleanup(Some(Set(Email1, Email2)), userAnswers).success.value

      result.get(NewEmailNotificationPage) mustBe None
    }

    "must retain NewEmailNotificationPage when Email3 is selected" in {
      val userAnswers = emptyUserAnswers
        .set(NewEmailNotificationPage, "new@email.com")
        .success
        .value

      val result = EmailSelectionPage.cleanup(Some(Set(Email1, Email3)), userAnswers).success.value

      result.get(NewEmailNotificationPage) mustBe defined
    }

    "must retain NewEmailNotificationPage when no value is provided" in {
      val userAnswers = emptyUserAnswers
        .set(NewEmailNotificationPage, "new@email.com")
        .success
        .value

      val result = EmailSelectionPage.cleanup(None, userAnswers).success.value

      result.get(NewEmailNotificationPage) mustBe defined
    }
  }
}
