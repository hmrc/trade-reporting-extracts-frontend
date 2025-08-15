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

package models.thirdparty

import base.SpecBase
import models.SectionNavigation
import pages.thirdparty.ThirdPartyDataOwnerConsentPage

class AddThirdPartySectionSpec extends SpecBase {

  "AddThirdPartySection" - {
    val section    = new AddThirdPartySection
    val sectionNav = SectionNavigation("addThirdPartySection")

    "navigateTo should return saved url if present" in {
      val answers = emptyUserAnswers.set(sectionNav, "/foo").success.value
      section.navigateTo(answers) mustBe "/foo"
    }

    "navigateTo should return initial page url if not present" in {
      section.navigateTo(emptyUserAnswers) mustBe section.initialPage.url
    }

    "saveNavigation should set urlFragment" in {
      val updatedAnswers = section.saveNavigation(emptyUserAnswers, "foo")
      updatedAnswers.get(sectionNav) mustBe Some("foo")
    }

    "removeAllAddThirdPartyAnswersAndNavigation should remove paths and report data" in {

      val userAnswers = emptyUserAnswers
        .set(sectionNav, "/foo")
        .success
        .value
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value

      val cleanedAnswers = AddThirdPartySection.removeAllAddThirdPartyAnswersAndNavigation(userAnswers)
      cleanedAnswers.get(sectionNav) mustBe None
      cleanedAnswers.get(ThirdPartyDataOwnerConsentPage) mustBe None
    }
  }

}
