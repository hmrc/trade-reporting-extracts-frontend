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

package navigation

import base.SpecBase
import models.{CheckMode, NormalMode}
import pages.thirdparty.{ThirdPartyAccessStartDatePage, ThirdPartyDataOwnerConsentPage, ThirdPartyReferencePage}

import java.time.LocalDate

class ThirdPartyNavigatorSpec extends SpecBase {

  val navigator = new ThirdPartyNavigator

  "ThirdPartyNavigator" - {

    "in Normal mode" - {

      "navigate from ThirdPartyDataOwnerConsentPage" - {
        "to next page when true" in {
          // TODO
        }
        "to CannotAddThirdParty when false" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyDataOwnerConsentPage, false).success.value
          navigator.nextPage(ThirdPartyDataOwnerConsentPage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        }
      }

      "navigate from third party reference page" - {

        "to ThirdPartyAccessStartDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyReferencePage, "ref").success.value
          navigator.nextPage(ThirdPartyReferencePage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(NormalMode)
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(ThirdPartyReferencePage, NormalMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "navigate from ThirdPartyAccessStartPage" - {

        "to ThirdPartyAccessEndDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyAccessStartDatePage, LocalDate.now()).success.value
          navigator.nextPage(ThirdPartyAccessStartDatePage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.ThirdPartyAccessEndDateController.onPageLoad(NormalMode)
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(ThirdPartyAccessStartDatePage, NormalMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }

    "in Check Mode" - {

      "navigate from ThirdPartyAccessStartPage" - {

        "to ThirdPartyAccessEndDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyAccessStartDatePage, LocalDate.now()).success.value
          navigator.nextPage(ThirdPartyAccessStartDatePage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.ThirdPartyAccessEndDateController.onPageLoad(CheckMode)
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(ThirdPartyAccessStartDatePage, CheckMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "navigate from third party reference page" - {

        "to CYA when answered" in {
          // TODO Change to CYA page
          val userAnswers = emptyUserAnswers.set(ThirdPartyReferencePage, "ref").success.value
          navigator.nextPage(ThirdPartyReferencePage, CheckMode, userAnswers) mustBe
            controllers.routes.DashboardController.onPageLoad()
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(ThirdPartyReferencePage, CheckMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }
  }
}
