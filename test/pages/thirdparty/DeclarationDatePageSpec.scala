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

package pages.thirdparty

import base.SpecBase
import models.thirdparty.DeclarationDate
import utils.json.OptionalLocalDateReads._

import java.time.LocalDate

class DeclarationDatePageSpec extends SpecBase {

  "cleanup" - {

    "must cleanup correctly when all data selection" in {

      val userAnswers = emptyUserAnswers
        .set(
          DataStartDatePage,
          LocalDate.of(2023, 1, 1)
        )
        .success
        .value
        .set(
          DataEndDatePage,
          Some(LocalDate.of(2023, 12, 31))
        )
        .success
        .value

      val cleanedUserAnswers =
        DeclarationDatePage.cleanup(Some(DeclarationDate.AllAvailableData), userAnswers).success.value

      cleanedUserAnswers.get(DataStartDatePage) mustBe None
      cleanedUserAnswers.get(DataEndDatePage) mustBe None
    }

    "must clean up correctly when custom date range selected" in {

      val userAnswers = emptyUserAnswers
        .set(
          DataStartDatePage,
          LocalDate.of(2023, 1, 1)
        )
        .success
        .value
        .set(
          DataEndDatePage,
          Some(LocalDate.of(2023, 12, 31))
        )
        .success
        .value

      val cleanedUserAnswers =
        DeclarationDatePage.cleanup(Some(DeclarationDate.CustomDateRange), userAnswers).success.value

      cleanedUserAnswers.get(DataStartDatePage) mustBe Some(LocalDate.of(2023, 1, 1))
      cleanedUserAnswers.get(DataEndDatePage).get mustBe Some(LocalDate.of(2023, 12, 31))
    }
  }
}
