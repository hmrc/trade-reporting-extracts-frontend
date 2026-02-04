/*
 * Copyright 2026 HM Revenue & Customs
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

package pages.additionalEmail

import base.SpecBase
import play.api.libs.json.JsPath

class NewAdditionalEmailPageSpec extends SpecBase {

  "NewAdditionalEmailPage" - {

    "must have the correct path" in {
      NewAdditionalEmailPage.path mustEqual JsPath \ "additionalEmail" \ "newAdditionalEmail"
    }

    "must have the correct toString" in {
      NewAdditionalEmailPage.toString mustEqual "newAdditionalEmail"
    }

    "must be able to bind string values" in {
      val emailAddress = "test@example.com"
      val userAnswers = emptyUserAnswers
        .set(NewAdditionalEmailPage, emailAddress)
        .success
        .value

      userAnswers.get(NewAdditionalEmailPage) mustBe Some(emailAddress)
    }

    "must be able to store and retrieve different email addresses" in {
      val emailAddress = "another@test.co.uk"
      val userAnswers = emptyUserAnswers
        .set(NewAdditionalEmailPage, emailAddress)
        .success
        .value

      userAnswers.get(NewAdditionalEmailPage) mustBe Some(emailAddress)
    }

    "must be able to remove values" in {
      val userAnswers = emptyUserAnswers
        .set(NewAdditionalEmailPage, "test@remove.com")
        .success
        .value
        .remove(NewAdditionalEmailPage)
        .success
        .value

      userAnswers.get(NewAdditionalEmailPage) mustBe None
    }

    "must handle empty string values" in {
      val userAnswers = emptyUserAnswers
        .set(NewAdditionalEmailPage, "")
        .success
        .value

      userAnswers.get(NewAdditionalEmailPage) mustBe Some("")
    }
  }
}