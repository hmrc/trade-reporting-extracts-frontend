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

package viewmodels.checkAnswers.thirdparty

import base.SpecBase
import models.{CheckMode, ThirdPartyDetails, UserAnswers}
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.thirdparty.{DataEndDatePage, DataStartDatePage}
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate

class DataTheyCanViewSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()
  implicit private val lang: Lang         = messages.lang

  "DataTheyCanViewSummary.checkYourAnswersRow" - {

    val startDate = LocalDate.of(2025, 6, 1)

    "when both start and end dates are answered, return the summary row for fixed period" in {
      val endDate     = LocalDate.of(2025, 6, 30)
      val userAnswers = UserAnswers("id")
        .set(DataStartDatePage, startDate)
        .get
        .set(DataEndDatePage, Some(endDate))
        .get

      DataTheyCanViewSummary.checkYourAnswersRow(userAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "dataTheyCanView.checkYourAnswersLabel",
          value = ValueViewModel(
            messages(
              "dataTheyCanView.fixed.answerLabel",
              startDate.format(dateTimeFormat()),
              endDate.format(dateTimeFormat())
            )
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.thirdparty.routes.DataStartDateController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("dataTheyCanView.change.hidden"))
          )
        )
      )
    }

    "when only start date is answered, return the summary row for ongoing period" in {

      val userAnswers = UserAnswers("id")
        .set(DataStartDatePage, startDate)
        .get

      DataTheyCanViewSummary.checkYourAnswersRow(userAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "dataTheyCanView.checkYourAnswersLabel",
          value = ValueViewModel(
            messages("dataTheyCanView.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.thirdparty.routes.DataStartDateController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("dataTheyCanView.change.hidden"))
          )
        )
      )
    }

    "when start date is not answered, return None" in {
      val userAnswers = UserAnswers("id")
      DataTheyCanViewSummary.checkYourAnswersRow(userAnswers) shouldBe None
    }
  }

  ".detailsRow" - {

    "when only start date available, return the summary row for ongoing period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = Some(startDate),
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      DataTheyCanViewSummary.detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "dataTheyCanView.checkYourAnswersLabel",
          value = ValueViewModel(
            messages("dataTheyCanView.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          )
        )
      )
    }

    "when both start and end dates available, return the summary row for fixed period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val endDate           = LocalDate.of(2025, 6, 30)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = Some(startDate),
        dataEndDate = Some(endDate),
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      DataTheyCanViewSummary.detailsRow(thirdPartyDetails, false, "thirdPartyEori", emptyUserAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "dataTheyCanView.checkYourAnswersLabel",
          value = ValueViewModel(
            messages(
              "dataTheyCanView.fixed.answerLabel",
              startDate.format(dateTimeFormat()),
              endDate.format(dateTimeFormat())
            )
          )
        )
      )
    }

    "when tpEnabledAndNotBusinessDetailsRow true, return the summary row with change action" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = Some(startDate),
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      DataTheyCanViewSummary.detailsRow(thirdPartyDetails, true, "thirdPartyEori", emptyUserAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "dataTheyCanView.checkYourAnswersLabel",
          value = ValueViewModel(
            messages("dataTheyCanView.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              "#"
            ).withVisuallyHiddenText(messages("dataTheyCanView.change.hidden"))
          )
        )
      )
    }
  }

  ".businessDetailsRow" - {

    "when only start date available, return the summary row for ongoing period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = Some(startDate),
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      DataTheyCanViewSummary.businessDetailsRow(thirdPartyDetails) shouldBe Some(
        SummaryListRowViewModel(
          key = "businessDetails.dataRange.label",
          value = ValueViewModel(
            messages("dataTheyCanView.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          )
        )
      )
    }

    "when both start and end dates available, return the summary row for fixed period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val endDate           = LocalDate.of(2025, 6, 30)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = Some(startDate),
        dataEndDate = Some(endDate),
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      DataTheyCanViewSummary.businessDetailsRow(thirdPartyDetails) shouldBe Some(
        SummaryListRowViewModel(
          key = "businessDetails.dataRange.label",
          value = ValueViewModel(
            messages(
              "dataTheyCanView.fixed.answerLabel",
              startDate.format(dateTimeFormat()),
              endDate.format(dateTimeFormat())
            )
          )
        )
      )
    }
  }
}
