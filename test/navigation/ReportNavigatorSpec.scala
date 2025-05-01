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
import models.*
import models.report._
import pages.*
import pages.report._
import java.time.{LocalDate, ZoneOffset}

class ReportNavigatorSpec extends SpecBase {

  val navigator = new FakeReportNavigation()

  "ReportNavigator" - {

    "in Normal mode" - {

      "DecisionPage must navigate to which-eori with any answer" in {

        val ua     = emptyUserAnswers
          .set(
            DecisionPage,
            Decision.values.head
          )
          .success
          .value
        val result = navigator.nextPage(DecisionPage, NormalMode, ua).url

        checkNavigation(result, "/which-eori")
      }

      "ChooseEoriPage must navigate to EoriRole when answered with Myeori" in {

        val ua     = emptyUserAnswers
          .set(
            ChooseEoriPage,
            ChooseEori.Myeori
          )
          .success
          .value
        val result = navigator.nextPage(ChooseEoriPage, NormalMode, ua).url

        checkNavigation(result, "/your-role")
      }

      "ChooseEoriPage must navigate to AccountsYouHaveAuthorityOverImport when answered with Myauthority" in {

        val ua     = emptyUserAnswers
          .set(
            ChooseEoriPage,
            ChooseEori.Myauthority
          )
          .success
          .value
        val result = navigator.nextPage(ChooseEoriPage, NormalMode, ua).url

        checkNavigation(result, "/accounts-you-have-authority-over-import")
      }

      "AccountsYouHaveAuthorityOverImportPage must navigate to ReportTypeImport with any answer" in {

        val ua     = emptyUserAnswers
          .set(
            AccountsYouHaveAuthorityOverImportPage,
            "eoriName"
          )
          .success
          .value
        val result = navigator.nextPage(EoriRolePage, NormalMode, ua).url

        checkNavigation(result, "/report-type")
      }

      "EoriRolePage must navigate to ReportTypeImport with any answer" in {

        val ua     = emptyUserAnswers
          .set(
            EoriRolePage,
            EoriRole.values.toSet
          )
          .success
          .value
        val result = navigator.nextPage(EoriRolePage, NormalMode, ua).url

        checkNavigation(result, "/report-type")
      }

      "ReportTypeImportPage must navigate to ReportDateRangePage with any answer" in {

        val ua     = emptyUserAnswers
          .set(
            ReportTypeImportPage,
            ReportTypeImport.values.toSet
          )
          .success
          .value
        val result = navigator.nextPage(ReportTypeImportPage, NormalMode, ua).url

        checkNavigation(result, "/date-rage")
      }

      "ReportNamePage must navigate to MaybeAdditionalEmail" in {

        val ua = emptyUserAnswers
          .set(ReportNamePage, "name")
          .success
          .value

        val result = navigator.nextPage(ReportNamePage, NormalMode, ua).url

        checkNavigation(result, "/choose-email-address")
      }

      "ReportDateRangePage" - {
        "when custom date range, navigate to CustomRequestStartDatePage" in {

          val ua = emptyUserAnswers
            .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
            .success
            .value

          val result = navigator.nextPage(ReportDateRangePage, NormalMode, ua).url

          checkNavigation(result, "/start-date")
        }

        "when last 31 days, navigate to ReportNamePage" in {

          val ua = emptyUserAnswers
            .set(ReportDateRangePage, ReportDateRange.Last31Days)
            .success
            .value

          val result = navigator.nextPage(ReportDateRangePage, NormalMode, ua).url

          checkNavigation(result, "/report-name")
        }

        "when last calendar month, navigate to ReportNamePage" in {

          val ua = emptyUserAnswers
            .set(ReportDateRangePage, ReportDateRange.LastCalendarMonth)
            .success
            .value

          val result = navigator.nextPage(ReportDateRangePage, NormalMode, ua).url

          checkNavigation(result, "/report-name")
        }
      }

      "customRequestStartDate" - {

        "when submitted must go to customRequestEndDatePage" in {

          val ua = emptyUserAnswers
            .set(CustomRequestStartDatePage, LocalDate.now(ZoneOffset.UTC).minusDays(5))
            .success
            .value

          val result = navigator.nextPage(CustomRequestStartDatePage, NormalMode, ua).url

          checkNavigation(result, "/end-date")
        }
      }

      "customRequestEndDate" - {

        "when submitted must go to reportNamePage" in {

          val ua = emptyUserAnswers
            .set(CustomRequestStartDatePage, LocalDate.now(ZoneOffset.UTC).minusDays(5))
            .success
            .value
            .set(CustomRequestEndDatePage, LocalDate.now(ZoneOffset.UTC).minusDays(4))
            .success
            .value

          val result = navigator.nextPage(CustomRequestEndDatePage, NormalMode, ua).url

          checkNavigation(result, "/report-name")
        }
      }
    }

    "MaybeAdditionalEmailPage must navigate to EmailSelectionPage" in {

      val ua = emptyUserAnswers
        .set(MaybeAdditionalEmailPage, true)
        .success
        .value

      val result = navigator.nextPage(MaybeAdditionalEmailPage, NormalMode, ua).url

      checkNavigation(result, "/notification-email")
    }

//:TODO this navigation need to be changed to the correct page once after implementation of other pages
    "MaybeAdditionalEmailPage must navigate to CheckYourAnswersController" in {

      val ua = emptyUserAnswers
        .set(MaybeAdditionalEmailPage, false)
        .success
        .value

      val result = navigator.nextPage(MaybeAdditionalEmailPage, NormalMode, ua).url

      checkNavigation(result, "/check-your-answers")
    }

    "EmailSelectionPage must navigate to NewEmailNotificationPage" in {
      val addEmailSelected: Set[EmailSelection] = Set(EmailSelection.Email3)
      val ua                                    = emptyUserAnswers
        .set(EmailSelectionPage, addEmailSelected)
        .success
        .value

      val result = navigator.nextPage(EmailSelectionPage, NormalMode, ua).url

      checkNavigation(result, "/new-notification-email")
    }
//:TODO this navigation need to be changed to the correct page once after implementation of other pages
    "EmailSelectionPage must navigate to CheckYourAnswersController" in {
      val emailSelected: Set[EmailSelection] = Set(EmailSelection.Email1)
      val ua                                 = emptyUserAnswers
        .set(EmailSelectionPage, emailSelected)
        .success
        .value

      val result = navigator.nextPage(EmailSelectionPage, NormalMode, ua).url

      checkNavigation(result, "/check-your-answers")
    }

    "in Check mode" - {

      "go to journey recovery in all instances" in {
        case object UnknownPage extends Page
        val result = navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")).url
        checkNavigation(result, "/problem/there-is-a-problem")
      }
    }
  }
}
