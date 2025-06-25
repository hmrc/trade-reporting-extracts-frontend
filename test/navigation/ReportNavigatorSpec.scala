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
import config.FrontendAppConfig
import controllers.report.routes
import models.report.{ChooseEori, Decision, EmailSelection, ReportDateRange}
import models.{CheckMode, NormalMode}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.report.*

class ReportNavigatorSpec extends SpecBase with MockitoSugar {

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.thirdPartyEnabled).thenReturn(true)
  when(mockAppConfig.notificationsEnabled).thenReturn(true)

  val navigator = new ReportNavigator(mockAppConfig)

  "ReportNavigator" - {

    "in Normal mode" - {

      "navigate from DecisionPage" in {
        val ua = emptyUserAnswers.set(DecisionPage, Decision.Import).success.value
        navigator.nextPage(DecisionPage, NormalMode, ua) mustBe routes.ChooseEoriController.onPageLoad(NormalMode)
      }

      "navigate from ChooseEoriPage" - {
        "to EoriRolePage when Myeori" in {
          val ua = emptyUserAnswers.set(ChooseEoriPage, ChooseEori.Myeori).success.value
          navigator.nextPage(ChooseEoriPage, NormalMode, ua) mustBe routes.EoriRoleController.onPageLoad(NormalMode)
        }

        "to AccountsYouHaveAuthorityOverImportPage when Myauthority" in {
          val ua = emptyUserAnswers.set(ChooseEoriPage, ChooseEori.Myauthority).success.value
          navigator.nextPage(ChooseEoriPage, NormalMode, ua) mustBe routes.AccountsYouHaveAuthorityOverImportController
            .onPageLoad(NormalMode)
        }
      }

      "navigate from AccountsYouHaveAuthorityOverImportPage" - {
        "to ReportTypeImportPage when Import" in {
          val ua = emptyUserAnswers.set(DecisionPage, Decision.Import).success.value
          navigator.nextPage(
            AccountsYouHaveAuthorityOverImportPage,
            NormalMode,
            ua
          ) mustBe routes.ReportTypeImportController.onPageLoad(NormalMode)
        }

        "to ReportDateRangePage when Export" in {
          val ua = emptyUserAnswers.set(DecisionPage, Decision.Export).success.value
          navigator.nextPage(
            AccountsYouHaveAuthorityOverImportPage,
            NormalMode,
            ua
          ) mustBe routes.ReportDateRangeController.onPageLoad(NormalMode)
        }
      }

      "navigate from EoriRolePage" - {
        "to ReportTypeImportPage when Import" in {
          val ua = emptyUserAnswers.set(DecisionPage, Decision.Import).success.value
          navigator.nextPage(EoriRolePage, NormalMode, ua) mustBe routes.ReportTypeImportController.onPageLoad(
            NormalMode
          )
        }

        "to ReportDateRangePage when Export" in {
          val ua = emptyUserAnswers.set(DecisionPage, Decision.Export).success.value
          navigator.nextPage(EoriRolePage, NormalMode, ua) mustBe routes.ReportDateRangeController.onPageLoad(
            NormalMode
          )
        }
      }

      "navigate from ReportTypeImportPage to ReportDateRangePage" in {
        navigator.nextPage(ReportTypeImportPage, NormalMode, emptyUserAnswers) mustBe routes.ReportDateRangeController
          .onPageLoad(NormalMode)
      }

      "navigate from ReportDateRangePage" - {
        "to CustomRequestStartDatePage when CustomDateRange" in {
          val ua = emptyUserAnswers.set(ReportDateRangePage, ReportDateRange.CustomDateRange).success.value
          navigator.nextPage(ReportDateRangePage, NormalMode, ua) mustBe routes.CustomRequestStartDateController
            .onPageLoad(NormalMode)
        }

        "to ReportNamePage otherwise" in {
          val ua = emptyUserAnswers.set(ReportDateRangePage, ReportDateRange.LastCalendarMonth).success.value
          navigator.nextPage(ReportDateRangePage, NormalMode, ua) mustBe routes.ReportNameController.onPageLoad(
            NormalMode
          )
        }
      }

      "navigate from CustomRequestStartDatePage to CustomRequestEndDatePage" in {
        navigator.nextPage(
          CustomRequestStartDatePage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CustomRequestEndDateController.onPageLoad(NormalMode)
      }

      "navigate from CustomRequestEndDatePage to ReportNamePage" in {
        navigator.nextPage(CustomRequestEndDatePage, NormalMode, emptyUserAnswers) mustBe routes.ReportNameController
          .onPageLoad(NormalMode)
      }

      "navigate from ReportNamePage to MaybeAdditionalEmailPage when notifications enabled" in {
        navigator.nextPage(ReportNamePage, NormalMode, emptyUserAnswers) mustBe routes.MaybeAdditionalEmailController
          .onPageLoad(NormalMode)
      }

      "navigate from MaybeAdditionalEmailPage" - {
        "to EmailSelectionPage when true" in {
          val ua = emptyUserAnswers.set(MaybeAdditionalEmailPage, true).success.value
          navigator.nextPage(MaybeAdditionalEmailPage, NormalMode, ua) mustBe routes.EmailSelectionController
            .onPageLoad(NormalMode)
        }

        "to CheckYourAnswersPage when false" in {
          val ua = emptyUserAnswers.set(MaybeAdditionalEmailPage, false).success.value
          navigator.nextPage(MaybeAdditionalEmailPage, NormalMode, ua) mustBe routes.CheckYourAnswersController
            .onPageLoad()
        }
      }

      "navigate from EmailSelectionPage" - {
        "to NewEmailNotificationPage when Email3 selected" in {
          val ua = emptyUserAnswers.set(EmailSelectionPage, Set(EmailSelection.Email3)).success.value
          navigator.nextPage(EmailSelectionPage, NormalMode, ua) mustBe routes.NewEmailNotificationController
            .onPageLoad(NormalMode)
        }

        "to CheckYourAnswersPage otherwise" in {
          val ua = emptyUserAnswers.set(EmailSelectionPage, Set(EmailSelection.Email1)).success.value
          navigator.nextPage(EmailSelectionPage, NormalMode, ua) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "navigate from NewEmailNotificationPage to CheckYourAnswersPage" in {
        navigator.nextPage(
          NewEmailNotificationPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "navigate from CheckYourAnswersPage to RequestConfirmationPage" in {
        navigator.nextPage(
          CheckYourAnswersPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.RequestConfirmationController.onPageLoad()
      }
    }

    "in Check mode" - {

      "navigate from DecisionPage to EoriRolePage" in {
        val ua = emptyUserAnswers.set(DecisionPage, Decision.Import).success.value
        navigator.nextPage(DecisionPage, CheckMode, ua) mustBe routes.EoriRoleController.onPageLoad(CheckMode)
      }

      "navigate from ChooseEoriPage" - {
        "to EoriRolePage when Myeori" in {
          val ua = emptyUserAnswers.set(ChooseEoriPage, ChooseEori.Myeori).success.value
          navigator.nextPage(ChooseEoriPage, CheckMode, ua) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "to AccountsYouHaveAuthorityOverImportPage when Myauthority" in {
          val ua = emptyUserAnswers.set(ChooseEoriPage, ChooseEori.Myauthority).success.value
          navigator.nextPage(ChooseEoriPage, CheckMode, ua) mustBe routes.AccountsYouHaveAuthorityOverImportController
            .onPageLoad(CheckMode)
        }
      }

      "navigate from AccountsYouHaveAuthorityOverImportPage to CheckYourAnswersPage" in {
        val ua = emptyUserAnswers.set(DecisionPage, Decision.Import).success.value
        navigator.nextPage(
          AccountsYouHaveAuthorityOverImportPage,
          CheckMode,
          ua
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "navigate from EoriRolePage" - {
        "to ReportTypeImportPage when Import" in {
          val ua = emptyUserAnswers.set(DecisionPage, Decision.Import).success.value
          navigator.nextPage(EoriRolePage, CheckMode, ua) mustBe routes.ReportTypeImportController.onPageLoad(CheckMode)
        }

        "to ReportDateRangePage when Export" in {
          val ua = emptyUserAnswers.set(DecisionPage, Decision.Export).success.value
          navigator.nextPage(EoriRolePage, CheckMode, ua) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "navigate from ReportTypeImportPage to CheckYourAnswersPage" in {
        navigator.nextPage(ReportTypeImportPage, CheckMode, emptyUserAnswers) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }

      "navigate from ReportDateRangePage" - {
        "to CustomRequestStartDatePage when CustomDateRange" in {
          val ua = emptyUserAnswers.set(ReportDateRangePage, ReportDateRange.CustomDateRange).success.value
          navigator.nextPage(ReportDateRangePage, CheckMode, ua) mustBe routes.CustomRequestStartDateController
            .onPageLoad(CheckMode)
        }

        "to CheckYourAnswersPage otherwise" in {
          val ua = emptyUserAnswers.set(ReportDateRangePage, ReportDateRange.LastCalendarMonth).success.value
          navigator.nextPage(ReportDateRangePage, CheckMode, ua) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "navigate from CustomRequestStartDatePage to CustomRequestEndDatePage" in {
        navigator.nextPage(
          CustomRequestStartDatePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CustomRequestEndDateController.onPageLoad(CheckMode)
      }

      "navigate from CustomRequestEndDatePage to CheckYourAnswersPage" in {
        navigator.nextPage(
          CustomRequestEndDatePage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "navigate from ReportNamePage to CheckYourAnswersPage" in {
        navigator.nextPage(ReportNamePage, CheckMode, emptyUserAnswers) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }

      "navigate from MaybeAdditionalEmailPage" - {
        "to EmailSelectionPage when true" in {
          val ua = emptyUserAnswers.set(MaybeAdditionalEmailPage, true).success.value
          navigator.nextPage(MaybeAdditionalEmailPage, CheckMode, ua) mustBe routes.EmailSelectionController.onPageLoad(
            CheckMode
          )
        }

        "to CheckYourAnswersPage when false" in {
          val ua = emptyUserAnswers.set(MaybeAdditionalEmailPage, false).success.value
          navigator.nextPage(MaybeAdditionalEmailPage, CheckMode, ua) mustBe routes.CheckYourAnswersController
            .onPageLoad()
        }
      }

      "navigate from EmailSelectionPage" - {
        "to NewEmailNotificationPage when Email3 selected" in {
          val ua = emptyUserAnswers.set(EmailSelectionPage, Set(EmailSelection.Email3)).success.value
          navigator.nextPage(EmailSelectionPage, CheckMode, ua) mustBe routes.NewEmailNotificationController.onPageLoad(
            CheckMode
          )
        }

        "to CheckYourAnswersPage otherwise" in {
          val ua = emptyUserAnswers.set(EmailSelectionPage, Set(EmailSelection.Email1)).success.value
          navigator.nextPage(EmailSelectionPage, CheckMode, ua) mustBe routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "navigate from NewEmailNotificationPage to CheckYourAnswersPage" in {
        navigator.nextPage(
          NewEmailNotificationPage,
          CheckMode,
          emptyUserAnswers
        ) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

    }
  }

}
