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
import models.{CheckMode, UserAnswers}
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.thirdparty.EoriNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class BusinessInfoSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "BusinessInfoSummary.row" - {

    "when answered, return the summary row" in {

      BusinessInfoSummary.row("business") shouldBe Some(
        SummaryListRowViewModel(
          key = "BusinessInfo.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("business").toString)
        )
      )
    }
  }
}
