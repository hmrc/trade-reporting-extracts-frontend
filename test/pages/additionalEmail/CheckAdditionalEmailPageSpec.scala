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
import play.api.libs.json.{JsPath, JsString}

class CheckAdditionalEmailPageSpec extends SpecBase {

  "CheckAdditionalEmailPage" - {

    "must have the correct path" in {
      CheckAdditionalEmailPage.path mustEqual JsPath \ "additionalEmail" \ "checkEmail"
    }

    "must have the correct toString" in {
      CheckAdditionalEmailPage.toString mustEqual "checkEmail"
    }

    "must be able to bind boolean values" in {
      val userAnswers = emptyUserAnswers
        .set(CheckAdditionalEmailPage, true)
        .success
        .value

      userAnswers.get(CheckAdditionalEmailPage) mustBe Some(true)
    }

    "must be able to store and retrieve false" in {
      val userAnswers = emptyUserAnswers
        .set(CheckAdditionalEmailPage, false)
        .success
        .value

      userAnswers.get(CheckAdditionalEmailPage) mustBe Some(false)
    }

    "must be able to remove values" in {
      val userAnswers = emptyUserAnswers
        .set(CheckAdditionalEmailPage, true)
        .success
        .value
        .remove(CheckAdditionalEmailPage)
        .success
        .value

      userAnswers.get(CheckAdditionalEmailPage) mustBe None
    }
  }
}