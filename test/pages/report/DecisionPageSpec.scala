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

package pages.report

import base.SpecBase
import models.report.Decision
import models.{EoriRole, ReportTypeImport}

import java.time.{LocalDate, ZoneOffset}

class DecisionPageSpec extends SpecBase {

  "cleanup" - {
    "must cleanup correctly" in {

      val ua = emptyUserAnswers
        .set(
          EoriRolePage,
          EoriRole.values.toSet
        )
        .success
        .value
        .set(
          ReportTypeImportPage,
          Set(ReportTypeImport.ExportItem)
        )
        .success
        .value

      val cleanedUserAnswers =
        DecisionPage.cleanup(Some(Decision.Import), ua).success.value

      cleanedUserAnswers.get(EoriRolePage) mustBe None
      cleanedUserAnswers.get(ReportTypeImportPage) mustBe None

    }
  }

}
