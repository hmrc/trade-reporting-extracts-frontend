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
import models.NormalMode
import models.thirdparty.DataTypes
import pages.thirdparty.{DataTypesPage, DeclarationDatePage, ThirdPartyDataOwnerConsentPage}

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

      "navigate from DataTypesPage" - {
        "to declarationDate page with any answer" in {
          val userAnswers = emptyUserAnswers.set(DataTypesPage, Set(DataTypes.Export)).success.value
          navigator.nextPage(DataTypesPage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.DeclarationDateController.onPageLoad(NormalMode)

        }
      }

      "navigate from declarationDate" - {
        "to next page when true" in {
          // TODO with TRE-594
        }
        "to CannotAddThirdParty when false" in {
          // TODO with TRE-591
        }
      }
    }

    "in Check Mode" - {}
  }
}
