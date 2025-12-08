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
import pages.editThirdParty.*
import models.thirdparty.DeclarationDate

class EditThirdPartyNavigatorSpec extends SpecBase {

  val navigator = new EditThirdPartyNavigator

  "ediThirdPartyNavigator" - {

    "EditThirdPartyDataTypesPage" - {

      "must go back to third party details screen" in {
        val page    = EditThirdPartyDataTypesPage("thirdPartyEori")
        val answers = emptyUserAnswers

        navigator.nextPage(page, userAnswers = answers) mustBe controllers.thirdparty.routes.ThirdPartyDetailsController
          .onPageLoad("thirdPartyEori")
      }

    }
    "EditThirdPartyAccessPeriodSummary" - {
      "EditThirdPartyAccessStartDatePage" - {
        "must go to edit third party end date screen" in {
          val page    = EditThirdPartyAccessStartDatePage("thirdPartyEori")
          val answers = emptyUserAnswers
          navigator.nextPage(
            page,
            userAnswers = answers
          ) mustBe controllers.editThirdParty.routes.EditThirdPartyAccessEndDateController
            .onPageLoad("thirdPartyEori")
        }
      }

      "EditThirdPartyAccessEndDatePage" - {
        "must go back to third party details screen" in {
          val page    = EditThirdPartyAccessEndDatePage("thirdPartyEori")
          val answers = emptyUserAnswers
          navigator.nextPage(
            page,
            userAnswers = answers
          ) mustBe controllers.thirdparty.routes.ThirdPartyDetailsController
            .onPageLoad("thirdPartyEori")
        }
      }
    }

    "EditThirdPartyReferencePage" - {

      "must go back to third party details screen" in {
        val page    = EditThirdPartyReferencePage("thirdPartyEori")
        val answers = emptyUserAnswers

        navigator.nextPage(page, userAnswers = answers) mustBe controllers.thirdparty.routes.ThirdPartyDetailsController
          .onPageLoad("thirdPartyEori")
      }

    }

    "EditDeclarationDatePage" - {

      "must go back to third party details when AllAvailableData selected" in {
        val page    = EditDeclarationDatePage("thirdPartyEori")
        val answers = emptyUserAnswers.set(page, DeclarationDate.AllAvailableData).success.value

        navigator.nextPage(page, userAnswers = answers) mustBe controllers.thirdparty.routes.ThirdPartyDetailsController
          .onPageLoad("thirdPartyEori")
      }

      "must go to edit data start date when CustomDateRange selected" in {
        val page    = EditDeclarationDatePage("thirdPartyEori")
        val answers = emptyUserAnswers.set(page, DeclarationDate.CustomDateRange).success.value

        navigator.nextPage(
          page,
          userAnswers = answers
        ) mustBe controllers.editThirdParty.routes.EditDataStartDateController
          .onPageLoad("thirdPartyEori")
      }

      "must go to journey recovery when no answer present" in {
        val page    = EditDeclarationDatePage("thirdPartyEori")
        val answers = emptyUserAnswers

        navigator.nextPage(page, userAnswers = answers) mustBe controllers.problem.routes.JourneyRecoveryController
          .onPageLoad()
      }
    }

    "EditDataStartDatePage" - {
      "must go to edit data end date screen" in {
        val page    = EditDataStartDatePage("thirdPartyEori")
        val answers = emptyUserAnswers

        navigator.nextPage(
          page,
          userAnswers = answers
        ) mustBe controllers.editThirdParty.routes.EditDataEndDateController
          .onPageLoad("thirdPartyEori")
      }
    }

    "EditDataEndDatePage" - {
      "must go back to third party details screen" in {
        val page    = EditDataEndDatePage("thirdPartyEori")
        val answers = emptyUserAnswers

        navigator.nextPage(page, userAnswers = answers) mustBe controllers.thirdparty.routes.ThirdPartyDetailsController
          .onPageLoad("thirdPartyEori")
      }
    }

  }

}
