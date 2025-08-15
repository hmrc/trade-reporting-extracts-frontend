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
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.thirdparty.ThirdPartyDataOwnerConsentPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class ThirdPartyDataOwnerConsentSummarySpec extends SpecBase {

  implicit private val messages: Messages = stubMessages()

  "ThirdPartyDataOwnerConsentSummary.row" - {

    "when answered, return the summary row" in {
      val userAnswers = UserAnswers("id")
        .set(
          ThirdPartyDataOwnerConsentPage,
          true
        )
        .get
      ThirdPartyDataOwnerConsentSummary.row(userAnswers) shouldBe Some(
        SummaryListRowViewModel(
          key = "thirdPartyDataOwnerConsent.checkYourAnswersLabel",
          value = ValueViewModel("thirdPartyDataOwnerConsent.yes"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.thirdparty.routes.ThirdPartyDataOwnerConsentController.onPageLoad(CheckMode).url
            )
              .withVisuallyHiddenText("thirdPartyDataOwnerConsent.change.hidden")
          )
        )
      )
    }

    "when answer unavailable, return empty" in {
      val userAnswers = UserAnswers("id")
      ThirdPartyDataOwnerConsentSummary.row(userAnswers) shouldBe None
    }

  }

}
