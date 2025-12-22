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

import models.UserAnswers
import models.{EoriRole, report}
import models.report.{ReportDateRange, ReportTypeImport}
import pages.report._

object ReportRequestFieldsValidator {

  sealed trait MissingField {
    def fieldKey: String
  }

  case object DecisionMissing extends MissingField {
    val fieldKey = "decision"
  }

  case object EoriRoleMissing extends MissingField {
    val fieldKey = "eoriRole"
  }

  case object ReportTypeImportMissing extends MissingField {
    val fieldKey = "reportTypeImport"
  }

  case object ReportDateRangeMissing extends MissingField {
    val fieldKey = "reportDateRange"
  }

  case object CustomRequestStartDateMissing extends MissingField {
    val fieldKey = "customRequestStartDate"
  }

  case object CustomRequestEndDateMissing extends MissingField {
    val fieldKey = "customRequestEndDate"
  }

  case object ReportNameMissing extends MissingField {
    val fieldKey = "reportName"
  }

  case class ValidationResult(isValid: Boolean, missingFields: Seq[MissingField])

  def validateMandatoryFields(userAnswers: UserAnswers, showDecisionSummary: Boolean): ValidationResult = {
    val missingFields = collectMissingFields(userAnswers, showDecisionSummary)
    ValidationResult(missingFields.isEmpty, missingFields)
  }

  private def collectMissingFields(userAnswers: UserAnswers, showDecisionSummary: Boolean): Seq[MissingField] = {
    val validators = Seq(
      () => validateDecision(userAnswers, showDecisionSummary),
      () => validateEoriRole(userAnswers),
      () => validateReportType(userAnswers),
      () => validateReportDateRange(userAnswers),
      () => validateCustomRequestStartDate(userAnswers),
      () => validateCustomRequestEndDate(userAnswers),
      () => validateReportName(userAnswers)
    )

    validators.flatMap(_.apply())
  }

  private def validateDecision(userAnswers: UserAnswers, showDecisionSummary: Boolean): Option[MissingField] =
    if (showDecisionSummary && userAnswers.get(DecisionPage).isEmpty) {
      Some(DecisionMissing)
    } else {
      None
    }

  private def validateEoriRole(userAnswers: UserAnswers): Option[MissingField] = {
    val isThirdParty = userAnswers.get(SelectThirdPartyEoriPage).isDefined
    if (!isThirdParty && userAnswers.get(EoriRolePage).isEmpty) Some(EoriRoleMissing) else None
  }

  private def validateReportType(userAnswers: UserAnswers): Option[MissingField] =
    if (userAnswers.get(ReportTypeImportPage).isEmpty) Some(ReportTypeImportMissing) else None

  private def validateReportDateRange(userAnswers: UserAnswers): Option[MissingField] =
    if (userAnswers.get(ReportDateRangePage).isEmpty) Some(ReportDateRangeMissing) else None

  private def validateCustomRequestStartDate(userAnswers: UserAnswers): Option[MissingField] =
    userAnswers.get(ReportDateRangePage) match {
      case Some(ReportDateRange.CustomDateRange) if userAnswers.get(CustomRequestStartDatePage).isEmpty =>
        Some(CustomRequestStartDateMissing)
      case _                                                                                            => None
    }

  private def validateCustomRequestEndDate(userAnswers: UserAnswers): Option[MissingField] =
    userAnswers.get(ReportDateRangePage) match {
      case Some(ReportDateRange.CustomDateRange) if userAnswers.get(CustomRequestEndDatePage).isEmpty =>
        Some(CustomRequestEndDateMissing)
      case _                                                                                          => None
    }

  private def validateReportName(userAnswers: UserAnswers): Option[MissingField] =
    if (userAnswers.get(ReportNamePage).isEmpty) Some(ReportNameMissing) else None

}
