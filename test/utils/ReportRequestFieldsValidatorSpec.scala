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

package utils

import base.SpecBase
import models.UserAnswers
import models.report.{Decision, ReportDateRange, ReportTypeImport}
import models.EoriRole
import pages.report._
import utils.ReportRequestFieldsValidator._

class ReportRequestFieldsValidatorSpec extends SpecBase {

  "ReportRequestFieldsValidator" - {

    "validateMandatoryFields" - {

      "should return valid result when all fields are present" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe true
        result.missingFields mustBe empty
      }

      "should return invalid result with missing fields when mandatory fields are missing" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
        // Missing: EoriRolePage, ReportTypeImportPage, ReportDateRangePage, ReportNamePage, MaybeAdditionalEmailPage

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe false
        result.missingFields.length mustBe 4
        result.missingFields must contain(EoriRoleMissing)
        result.missingFields must contain(ReportNameMissing)
        result.missingFields must contain(ReportDateRangeMissing)
        result.missingFields must contain(ReportTypeImportMissing)
      }

      "should not require DecisionSummary when showDecisionSummary is false" in {
        val userAnswers = emptyUserAnswers
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = false)

        result.isValid mustBe true
        result.missingFields mustBe empty
      }

      "should require CustomRequestStartDate when ReportDateRange is CustomDateRange but start date is missing" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value
          .set(CustomRequestEndDatePage, java.time.LocalDate.of(2025, 1, 31))
          .success
          .value
        val result      = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe false
        result.missingFields must contain(CustomRequestStartDateMissing)
      }

      "should require CustomRequestEndDate when ReportDateRange is CustomDateRange but end date is missing" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value
          .set(CustomRequestStartDatePage, java.time.LocalDate.of(2025, 1, 1))
          .success
          .value
        // Missing: CustomRequestEndDatePage

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe false
        result.missingFields must contain(CustomRequestEndDateMissing)
      }

      "should require both CustomRequestStartDate and CustomRequestEndDate when ReportDateRange is CustomDateRange but both dates are missing" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value
        val result      = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe false
        result.missingFields must contain(CustomRequestStartDateMissing)
        result.missingFields must contain(CustomRequestEndDateMissing)
      }

      "should be valid when ReportDateRange is CustomDateRange and both custom dates are present" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value
          .set(CustomRequestStartDatePage, java.time.LocalDate.of(2025, 1, 1))
          .success
          .value
          .set(CustomRequestEndDatePage, java.time.LocalDate.of(2025, 1, 31))
          .success
          .value

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe true
        result.missingFields mustBe empty
      }

      "should not require custom dates when ReportDateRange is not CustomDateRange" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.LastFullCalendarMonth)
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value
        val result      = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result.isValid mustBe true
        result.missingFields mustBe empty
      }
    }
  }
}
