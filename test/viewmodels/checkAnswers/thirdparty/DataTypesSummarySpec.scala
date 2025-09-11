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
import models.{ThirdPartyDetails, UserAnswers}
import models.thirdparty.DataTypes
import pages.thirdparty.DataTypesPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import java.time.LocalDate

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

      val result = DataTypesSummary.detailsRow(Set("imports")).get

      result.key.content.asHtml.body   must include("thirdPartyDetails.dataTypes.label")
      result.value.content.asHtml.body must include(messages("dataTypes.import"))
    }

    "must return summary list row when given multiple data types" in {

      val result = DataTypesSummary.detailsRow(Set("imports", "exports")).get

      result.key.content.asHtml.body   must include("thirdPartyDetails.dataTypes.label")
      result.value.content.asHtml.body must include(messages("dataTypes.import"))
      result.value.content.asHtml.body must include(messages("dataTypes.export"))
    }
  }
}
