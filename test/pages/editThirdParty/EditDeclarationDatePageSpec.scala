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

package pages.editThirdParty

import base.SpecBase
import models.thirdparty.DeclarationDate
import utils.json.OptionalLocalDateReads.*

import java.time.LocalDate

class EditDeclarationDatePageSpec extends SpecBase {

  "cleanup" - {

    "must cleanup correctly when all data selection" in {

      val userAnswers = emptyUserAnswers
        .set(
          EditDataStartDatePage("thirpartyeori"),
          LocalDate.of(2023, 1, 1)
        )
        .success
        .value
        .set(
          EditDataEndDatePage("thirpartyeori"),
          Some(LocalDate.of(2023, 12, 31))
        )
        .success
        .value

      val cleanedUserAnswers =
        EditDeclarationDatePage("thirpartyeori")
          .cleanup(Some(DeclarationDate.AllAvailableData), userAnswers)
          .success
          .value

      cleanedUserAnswers.get(EditDataStartDatePage("thirpartyeori")) mustBe None
      cleanedUserAnswers.get(EditDataEndDatePage("thirpartyeori")) mustBe None
    }

    "must clean up correctly when custom date range selected" in {

      val userAnswers = emptyUserAnswers
        .set(
          EditDataStartDatePage("thirpartyeori"),
          LocalDate.of(2023, 1, 1)
        )
        .success
        .value
        .set(
          EditDataEndDatePage("thirpartyeori"),
          Some(LocalDate.of(2023, 12, 31))
        )
        .success
        .value

      val cleanedUserAnswers =
        EditDeclarationDatePage("thirpartyeori")
          .cleanup(Some(DeclarationDate.CustomDateRange), userAnswers)
          .success
          .value

      cleanedUserAnswers.get(EditDataStartDatePage("thirpartyeori")) mustBe Some(LocalDate.of(2023, 1, 1))
      cleanedUserAnswers.get(EditDataEndDatePage("thirpartyeori")).get mustBe Some(LocalDate.of(2023, 12, 31))
    }
  }
}
