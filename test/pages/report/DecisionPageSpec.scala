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
import models.EoriRole
import models.report.{Decision, ReportTypeImport}

class DecisionPageSpec extends SpecBase {

  "DecisionPage.cleanup" - {

    "must remove EoriRolePage and ReportTypeImportPage when a decision is provided" in {
      val userAnswers = emptyUserAnswers
        .set(EoriRolePage, EoriRole.values.toSet)
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem))
        .success
        .value

      val result = DecisionPage.cleanup(Some(Decision.Import), userAnswers).success.value

      result.get(EoriRolePage) mustBe None
      result.get(ReportTypeImportPage) mustBe None
    }

    "must not remove any pages when no decision is provided (None)" in {
      val userAnswers = emptyUserAnswers
        .set(EoriRolePage, EoriRole.values.toSet)
        .success
        .value
        .set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem))
        .success
        .value

      val result = DecisionPage.cleanup(None, userAnswers).success.value

      result.get(EoriRolePage) mustBe defined
      result.get(ReportTypeImportPage) mustBe defined
    }
  }
}
