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
import models.report.{Decision, ReportDateRange, ReportTypeImport}
import models.EoriRole
import pages.report._

class ReportRequestFieldsValidatorSpec extends SpecBase {

  "ReportRequestFieldsValidator" - {

    "validateMandatoryFields" - {

      "should return true when all fields are present" in {
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

        result mustBe true
      }

      "should return false when mandatory fields are missing" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
        // Missing: EoriRolePage, ReportTypeImportPage, ReportDateRangePage, ReportNamePage, MaybeAdditionalEmailPage

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result mustBe false
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

        result mustBe true
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
        // Missing CustomRequestStartDatePage

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result mustBe false
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
        // Missing CustomRequestEndDatePage

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result mustBe false
      }

      "should return true when custom date range is provided with both start and end dates" in {
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

        result mustBe true
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

        result mustBe true
      }
    }

    "Security Validations" - {

      "should detect Export decision with Import report types mismatch" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Export)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Exporter))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem, ReportTypeImport.ImportHeader))
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

        result mustBe false
      }

      "should detect Import decision with Export report types mismatch" in {
        val userAnswers = emptyUserAnswers
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Importer))
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ExportItem))
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

        result mustBe false
      }

      "should allow valid decision-report type combinations" in {
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

        result mustBe true
      }

      "should prevent third party users from selecting Declarant role" in {
        val userAnswers = emptyUserAnswers
          .set(SelectThirdPartyEoriPage, "GB123456789000")
          .success
          .value
          .set(EoriRolePage, Set(EoriRole.Declarant))
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

        result mustBe false
      }

      "should prevent third party users from having report date range data" in {
        val userAnswers = emptyUserAnswers
          .set(SelectThirdPartyEoriPage, "GB123456789000")
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

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = false)

        result mustBe false
      }

      "should allow third party users without EoriRole requirement" in {
        val userAnswers = emptyUserAnswers
          .set(SelectThirdPartyEoriPage, "GB123456789000")
          .success
          .value
          .set(DecisionPage, Decision.Import)
          .success
          .value
          .set(ReportTypeImportPage, Set(ReportTypeImport.ImportItem))
          .success
          .value
          .set(ReportDateRangePage, ReportDateRange.CustomDateRange)
          .success
          .value
          .set(CustomRequestStartDatePage, java.time.LocalDate.of(2025, 1, 1))
          .success
          .value
          .set(CustomRequestEndDatePage, java.time.LocalDate.of(2025, 1, 31))
          .success
          .value
          .set(ReportNamePage, "Test Report")
          .success
          .value
          .set(MaybeAdditionalEmailPage, false)
          .success
          .value

        val result = ReportRequestFieldsValidator.validateMandatoryFields(userAnswers, showDecisionSummary = true)

        result mustBe true
      }
    }
  }
}
