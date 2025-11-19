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
import models.UserAnswers
import models.thirdparty.DataTypes
import pages.thirdparty.DataTypesPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class DataTypesSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "DataTypesSummary row" - {

    "must return a SummaryListRow when DataTypesPage has values" in {
      val answerSet   = Set(DataTypes.values.head) // or any subset of DataTypes
      val userAnswers = UserAnswers("id")
        .set(DataTypesPage, answerSet)
        .success
        .value

      val result = DataTypesSummary.checkYourAnswersRow(userAnswers)

      result mustBe defined
      result.get.key.content.asHtml.body   must include("dataTypes.checkYourAnswersLabel")
      result.get.value.content.asHtml.body must include(messages("dataTypes.import"))
    }

    "must return None when DataTypesPage is not defined" in {
      val userAnswers = UserAnswers("id")

      val result = DataTypesSummary.checkYourAnswersRow(userAnswers)

      result mustBe None
    }
  }

  ".detailsRow" - {
    "must return summary list row when given single data type" in {

      val result = DataTypesSummary.detailsRow(Set("imports"), false, "thirdPartyEori").get

      result.key.content.asHtml.body   must include("thirdPartyDetails.dataTypes.label")
      result.value.content.asHtml.body must include(messages("dataTypes.import"))
    }

    "must return summary list row when given multiple data types" in {

      val result = DataTypesSummary.detailsRow(Set("imports", "exports"), false, "thirdPartyEori").get

      result.key.content.asHtml.body   must include("thirdPartyDetails.dataTypes.label")
      result.value.content.asHtml.body must include(messages("dataTypes.import"))
      result.value.content.asHtml.body must include(messages("dataTypes.export"))
    }

    "when tpEnabledAndNotBusinessDetailsRow true, return the summary row with change action" in {
      val result = DataTypesSummary.detailsRow(Set("imports", "exports"), true, "thirdPartyEori").get

      result mustBe
        SummaryListRowViewModel(
          key = "thirdPartyDetails.dataTypes.label",
          value = ValueViewModel(HtmlContent("dataTypes.import,<br>dataTypes.export")),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              "/request-customs-declaration-data/editThirdPartyDataTypes/thirdPartyEori"
            ).withVisuallyHiddenText(messages("dataTypes.change.hidden"))
          )
        )

    }

    "when tpEnabledAndNotBusinessDetailsRow false, return the summary row without change action" in {
      val result = DataTypesSummary.detailsRow(Set("imports", "exports"), false, "thirdPartyEori").get

      result mustBe
        SummaryListRowViewModel(
          key = "thirdPartyDetails.dataTypes.label",
          value = ValueViewModel(HtmlContent("dataTypes.import,<br>dataTypes.export"))
        )
    }

  }

  ".businessDetailsRow" - {
    "must return summary list row when given single data type" in {

      val result = DataTypesSummary.businessDetailsRow(Set("imports")).get

      result.key.content.asHtml.body   must include("businessDetails.dataTypes.label")
      result.value.content.asHtml.body must include(messages("dataTypes.import"))
    }

    "must return summary list row when given multiple data types" in {

      val result = DataTypesSummary.businessDetailsRow(Set("imports", "exports")).get

      result.key.content.asHtml.body   must include("businessDetails.dataTypes.label")
      result.value.content.asHtml.body must include(messages("dataTypes.import"))
      result.value.content.asHtml.body must include(messages("dataTypes.export"))
    }
  }
}
