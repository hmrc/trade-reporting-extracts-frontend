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
import pages.thirdparty.{ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage}
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate

class ThirdPartyAccessPeriodSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()
  implicit private val lang: Lang         = messages.lang

  "ThirdPartyAccessPeriodSummary.checkYourAnswersRow" - {

    val startDate = LocalDate.of(2025, 6, 1)

    "when both start and end dates are answered, return the summary row for fixed period" in {
      val endDate     = LocalDate.of(2025, 6, 30)
      val userAnswers = UserAnswers("id")
        .set(ThirdPartyAccessStartDatePage, startDate)
        .get
        .set(ThirdPartyAccessEndDatePage, Some(endDate))
        .get

      ThirdPartyAccessPeriodSummary.checkYourAnswersRow(userAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            messages(
              "thirdPartyAccessPeriod.fixed.answerLabel",
              startDate.format(dateTimeFormat()),
              endDate.format(dateTimeFormat())
            )
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("thirdPartyAccessPeriod.change.hidden"))
          )
        )
      )
    }

    "when only start date is answered, return the summary row for ongoing period" in {

      val userAnswers = UserAnswers("id")
        .set(ThirdPartyAccessStartDatePage, startDate)
        .get

      ThirdPartyAccessPeriodSummary.checkYourAnswersRow(userAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            messages("thirdPartyAccessPeriod.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(messages("thirdPartyAccessPeriod.change.hidden"))
          )
        )
      )
    }

    "when start date is not answered, return None" in {
      val userAnswers = UserAnswers("id")
      ThirdPartyAccessPeriodSummary.checkYourAnswersRow(userAnswers) shouldBe None
    }
  }

  ".detailsRow" - {

    "when both start and end dates are provided, return summary list row for fixed period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val endDate           = LocalDate.of(2025, 6, 30)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = None,
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = Some(endDate),
        dataTypes = Set("import")
      )

      val result = ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails)

      result shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            messages(
              "thirdPartyAccessPeriod.fixed.answerLabel",
              startDate.format(dateTimeFormat()),
              endDate.format(dateTimeFormat())
            )
          )
        )
      )
    }

    "when only start date is provided, return summary list row for ongoing period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = None,
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      val result = ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails)

      result shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            messages("thirdPartyAccessPeriod.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          )
        )
      )
    }
  }

  ".businessDetailsRow" - {

    "when both start and end dates are provided, return summary list row for fixed period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val endDate           = LocalDate.of(2025, 6, 30)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = None,
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = Some(endDate),
        dataTypes = Set("import")
      )

      val result = ThirdPartyAccessPeriodSummary.businessDetailsRow(thirdPartyDetails)

      result shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            messages(
              "thirdPartyAccessPeriod.fixed.answerLabel",
              startDate.format(dateTimeFormat()),
              endDate.format(dateTimeFormat())
            )
          )
        )
      )
    }

    "when only start date is provided, return summary list row for ongoing period" in {
      val startDate         = LocalDate.of(2025, 6, 1)
      val thirdPartyDetails = ThirdPartyDetails(
        dataStartDate = None,
        dataEndDate = None,
        referenceName = None,
        accessStartDate = startDate,
        accessEndDate = None,
        dataTypes = Set("import")
      )

      val result = ThirdPartyAccessPeriodSummary.businessDetailsRow(thirdPartyDetails)

      result shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
          value = ValueViewModel(
            messages("thirdPartyAccessPeriod.ongoing.answerLabel", startDate.format(dateTimeFormat()))
          )
        )
      )
    }
  }
}
