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

class NewEmailNotificationPageSpec extends SpecBase {

  "NewEmailNotificationPage" - {
    "must remove CheckNewEmailPage when answered" in {
      val userAnswers = UserAnswers("id")
        .set(CheckNewEmailPage, true)
        .success
        .value

      val result = NewEmailNotificationPage.cleanup(Some("email"), userAnswers).success.value

      result.get(NewEmailNotificationPage) must not be defined
    }

  }
}
