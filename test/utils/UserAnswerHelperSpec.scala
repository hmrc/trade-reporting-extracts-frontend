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

package utils

import base.SpecBase
import pages.editThirdParty.EditThirdPartyReferencePage

class UserAnswerHelperSpec extends SpecBase {

  "must remove edit third party answers for a given eori" in {
    val helper      = new UserAnswerHelper()
    val userAnswers = emptyUserAnswers.set(EditThirdPartyReferencePage("eori1"), "someRef").get

    val cleanedAnswers = helper.removeEditThirdPartyAnswersForEori("eori1", userAnswers)
    cleanedAnswers.get(EditThirdPartyReferencePage("eori1")) mustBe None
  }

  "must not remove other third party answers when removing for a given eori" in {
    val helper      = new UserAnswerHelper()
    val userAnswers = emptyUserAnswers
      .set(EditThirdPartyReferencePage("eori1"), "someRef")
      .get
      .set(EditThirdPartyReferencePage("eori2"), "anotherRef")
      .get

    val cleanedAnswers = helper.removeEditThirdPartyAnswersForEori("eori1", userAnswers)
    cleanedAnswers.get(EditThirdPartyReferencePage("eori1")) mustBe None
    cleanedAnswers.get(EditThirdPartyReferencePage("eori2")) mustBe Some("anotherRef")
  }

}
