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
import pages.thirdparty.{DataEndDatePage, DataStartDatePage, ThirdPartyDataOwnerConsentPage}
import java.time.LocalDate
class ThirdPartyNavigatorSpec extends SpecBase {

  val navigator = new ThirdPartyNavigator

  "ThirdPartyNavigator" - {

    "in Normal mode" - {

      "navigate from ThirdPartyDataOwnerConsentPage" - {
        "to next page when true" in {
          // TODO COMPLETE
        }
        "to CannotAddThirdParty when false" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyDataOwnerConsentPage, false).success.value
          navigator.nextPage(ThirdPartyDataOwnerConsentPage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        }
      }

      "navigate from DataStartDatePage" - {

        "to DataEndDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(DataStartDatePage, LocalDate.now()).success.value
          navigator.nextPage(DataStartDatePage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.DataEndDateController.onPageLoad(NormalMode)
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(DataStartDatePage, NormalMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "navigate from DataEndDatePage" - {

        "to DataEndDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(DataEndDatePage, Option(LocalDate.now())).success.value
          // TODO complete with check your answers page when available
        }
      }
    }

    "in Check Mode" - {
      "navigate from DataStartDatePage" - {

        "to DataEndDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(DataStartDatePage, LocalDate.now()).success.value
          navigator.nextPage(DataStartDatePage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.DataEndDateController.onPageLoad(CheckMode)
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(DataStartDatePage, CheckMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }

      "navigate from DataEndDatePage" - {

        "to DataEndDatePage when answered" in {
          val userAnswers = emptyUserAnswers.set(DataEndDatePage, Option(LocalDate.now())).success.value
          // TODO complete with check your answers page when available
        }

      }

    }
  }
}
