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
import pages.thirdparty.{ConfirmEoriPage, DataEndDatePage, DataStartDatePage, DataTypesPage, DeclarationDatePage, ThirdPartyAccessStartDatePage, ThirdPartyDataOwnerConsentPage, ThirdPartyReferencePage}
import models.thirdparty.{ConfirmEori, DataTypes, DeclarationDate}

import java.time.LocalDate

class ThirdPartyNavigatorSpec extends SpecBase {

  val navigator = new ThirdPartyNavigator

  "ThirdPartyNavigator" - {

    "in Normal mode" - {

      "navigate from ThirdPartyDataOwnerConsentPage" - {
        "to EORI number page when true" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyDataOwnerConsentPage, true).success.value
          navigator.nextPage(ThirdPartyDataOwnerConsentPage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.EoriNumberController.onPageLoad(NormalMode)
        }
        "to CannotAddThirdParty when false" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyDataOwnerConsentPage, false).success.value
          navigator.nextPage(ThirdPartyDataOwnerConsentPage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        }
      }

      "navigate from EoriNumberPage" - {
        "to confirm eori page with any answer" in {
          val userAnswers = emptyUserAnswers.set(pages.thirdparty.EoriNumberPage, "GB123456789000").success.value
          navigator.nextPage(pages.thirdparty.EoriNumberPage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.ConfirmEoriController.onPageLoad(NormalMode)
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

      "navigate from ThirdPartyAccessEndPage" - {

        "NormalMode" - {
          "to DataTypesPage when answered" in {
            val userAnswers =
              emptyUserAnswers.set(pages.thirdparty.ThirdPartyAccessEndDatePage, Option(LocalDate.now())).success.value
            navigator.nextPage(pages.thirdparty.ThirdPartyAccessEndDatePage, NormalMode, userAnswers) mustBe
              controllers.thirdparty.routes.DataTypesController.onPageLoad(NormalMode)
          }
        }

        "CheckMode" - {
          "should go to AddThirdPartyCheckYourAnswersController when DataTypesPage is answered" in {
            val userAnswers = emptyUserAnswers
              .set(pages.thirdparty.DataTypesPage, Set(models.thirdparty.DataTypes.Export))
              .success
              .value
            navigator.nextPage(pages.thirdparty.ThirdPartyAccessEndDatePage, CheckMode, userAnswers) mustBe
              controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
          }
          "should go to DataTypesController in CheckMode when DataTypesPage is not answered" in {
            val userAnswers = emptyUserAnswers
            navigator.nextPage(pages.thirdparty.ThirdPartyAccessEndDatePage, CheckMode, userAnswers) mustBe
              controllers.thirdparty.routes.DataTypesController.onPageLoad(CheckMode)
          }
        }
      }

      "navigate from DataTypesPage" - {
        "NormalMode" - {
          "to declarationDate page with any answer" in {
            val userAnswers = emptyUserAnswers.set(DataTypesPage, Set(DataTypes.Export)).success.value
            navigator.nextPage(DataTypesPage, NormalMode, userAnswers) mustBe
              controllers.thirdparty.routes.DeclarationDateController.onPageLoad(NormalMode)
          }
        }
        "CheckMode" - {
          "should go to DeclarationDateController in CheckMode when DeclarationDatePage is answered" in {
            val userAnswers = emptyUserAnswers
              .set(pages.thirdparty.DeclarationDatePage, models.thirdparty.DeclarationDate.AllAvailableData)
              .success
              .value
            navigator.nextPage(pages.thirdparty.DataTypesPage, CheckMode, userAnswers) mustBe
              controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
          }
          "should go to AddThirdPartyCheckYourAnswersController when DeclarationDatePage is not answered" in {
            val userAnswers = emptyUserAnswers
            navigator.nextPage(pages.thirdparty.DataTypesPage, CheckMode, userAnswers) mustBe
              controllers.thirdparty.routes.DeclarationDateController.onPageLoad(CheckMode)
          }
        }
      }

      "navigate from declarationDate" - {
        "to Check your answers when AllAvailableData" in {
          val ua = emptyUserAnswers.set(DeclarationDatePage, DeclarationDate.AllAvailableData).success.value
          navigator.nextPage(
            DeclarationDatePage,
            NormalMode,
            ua
          ) mustBe controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController
            .onPageLoad()

        }
        "to DataStartDateController when CustomDateRange answered" in {
          val ua = emptyUserAnswers.set(DeclarationDatePage, DeclarationDate.CustomDateRange).success.value
          navigator.nextPage(
            DeclarationDatePage,
            NormalMode,
            ua
          ) mustBe controllers.thirdparty.routes.DataStartDateController.onPageLoad(NormalMode)

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
          navigator.nextPage(DataEndDatePage, NormalMode, userAnswers) mustBe
            controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
        }
      }
    }

    "in Check Mode" - {

      "navigate from ThirdPartyDataOwnerConsentPage" - {
        "to eori number controller in check mode when true" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyDataOwnerConsentPage, true).success.value
          navigator.nextPage(ThirdPartyDataOwnerConsentPage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.EoriNumberController.onPageLoad(CheckMode)
        }
        "to CannotAddThirdParty when false" in {
          val userAnswers = emptyUserAnswers.set(ThirdPartyDataOwnerConsentPage, false).success.value
          navigator.nextPage(ThirdPartyDataOwnerConsentPage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        }
      }

      "navigate from EoriNumberPage" - {
        "to confirm Eori page in Normal mode with any answer" in {
          val userAnswers = emptyUserAnswers.set(pages.thirdparty.EoriNumberPage, "GB123456789000").success.value
          navigator.nextPage(pages.thirdparty.EoriNumberPage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.ConfirmEoriController.onPageLoad(CheckMode)
        }
      }

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
          val userAnswers = emptyUserAnswers.set(ThirdPartyReferencePage, "ref").success.value
          navigator.nextPage(ThirdPartyReferencePage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
        }

        "to journey recovery when not answered" in {
          val userAnswers = emptyUserAnswers
          navigator.nextPage(ThirdPartyReferencePage, CheckMode, userAnswers) mustBe
            controllers.problem.routes.JourneyRecoveryController.onPageLoad()
        }
      }

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
          navigator.nextPage(DataEndDatePage, CheckMode, userAnswers) mustBe
            controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
        }
      }
    }

    "ThirdPartyNavigator.nextPage with skipFlag" - {

      "redirect to ThirdPartyAccessStartDateController when ConfirmEori is Yes and skipFlag is true" in {
        val userAnswers = emptyUserAnswers.set(ConfirmEoriPage, ConfirmEori.Yes).success.value
        val result      = navigator.nextPage(ConfirmEoriPage, NormalMode, userAnswers, true)

        result.url mustBe controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(NormalMode).url
      }

      "redirect to ThirdPartyReferenceController when ConfirmEori is Yes and skipFlag is false" in {
        val userAnswers = emptyUserAnswers.set(ConfirmEoriPage, ConfirmEori.Yes).success.value
        val result      = navigator.nextPage(ConfirmEoriPage, NormalMode, userAnswers, false)

        result.url mustBe controllers.thirdparty.routes.ThirdPartyReferenceController.onPageLoad(NormalMode).url
      }

      "redirect to EoriNumberController when ConfirmEori is No" in {
        val userAnswers = emptyUserAnswers.set(ConfirmEoriPage, ConfirmEori.No).success.value
        val result      = navigator.nextPage(ConfirmEoriPage, NormalMode, userAnswers, false)

        result.url mustBe controllers.thirdparty.routes.EoriNumberController.onPageLoad(NormalMode).url
      }

      "redirect to JourneyRecoveryController when ConfirmEori is missing" in {
        val result = navigator.nextPage(ConfirmEoriPage, NormalMode, emptyUserAnswers, false)

        result.url mustBe controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
