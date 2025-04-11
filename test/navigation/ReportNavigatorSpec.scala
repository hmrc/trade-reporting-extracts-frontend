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
import controllers.routes
import models.*
import models.report.{ChooseEori, Decision}
import pages.*
import pages.report.{ChooseEoriPage, DecisionPage, EoriRolePage, ReportNamePage, ReportTypeImportPage}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

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

      "ChooseEoriPage must navigate to EoriRole with any answer" in {

        val ua     = emptyUserAnswers
          .set(
            ChooseEoriPage,
            ChooseEori.values.head
          )
          .success
          .value
        val result = navigator.nextPage(ChooseEoriPage, NormalMode, ua).url

        checkNavigation(result, "/request-cds-report/eoriRole")
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
