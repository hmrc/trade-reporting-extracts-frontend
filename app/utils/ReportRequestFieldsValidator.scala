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
import pages.report.*

object ReportRequestFieldsValidator {

  def validateMandatoryFields(userAnswers: UserAnswers, showDecisionSummary: Boolean): Boolean = {
    val validations = Seq(
      validateDecision(userAnswers, showDecisionSummary),
      validateEoriRole(userAnswers),
      validateReportType(userAnswers),
      validateReportDateRange(userAnswers),
      validateCustomRequestStartDate(userAnswers),
      validateCustomRequestEndDate(userAnswers),
      validateReportName(userAnswers),
      validateRoleReportTypeConsistency(userAnswers),
      validateThirdPartyAccess(userAnswers)
    )

    validations.forall(identity)
  }

  private def validateDecision(userAnswers: UserAnswers, showDecisionSummary: Boolean): Boolean =
    !showDecisionSummary || userAnswers.get(DecisionPage).isDefined

  private def validateEoriRole(userAnswers: UserAnswers): Boolean = {
    val isThirdParty = userAnswers.get(SelectThirdPartyEoriPage).isDefined
    isThirdParty || userAnswers.get(EoriRolePage).isDefined
  }

  private def validateReportType(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportTypeImportPage).isDefined

  private def validateReportDateRange(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportDateRangePage).isDefined

  private def validateCustomRequestStartDate(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportDateRangePage) match {
      case Some(ReportDateRange.CustomDateRange) => userAnswers.get(CustomRequestStartDatePage).isDefined
      case _                                     => true
    }

  private def validateCustomRequestEndDate(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportDateRangePage) match {
      case Some(ReportDateRange.CustomDateRange) => userAnswers.get(CustomRequestEndDatePage).isDefined
      case _                                     => true
    }

  private def validateReportName(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportNamePage).isDefined

  private def validateRoleReportTypeConsistency(userAnswers: UserAnswers): Boolean = {
    val decision    = userAnswers.get(DecisionPage)
    val reportTypes = userAnswers.get(ReportTypeImportPage).getOrElse(Set.empty)

    decision match {
      case Some(decision) =>
        val hasExportDecision = decision.toString.toLowerCase == "export"
        val hasImportTypes    = reportTypes.exists(reportTyp =>
          reportTyp == ReportTypeImport.ImportHeader ||
            reportTyp == ReportTypeImport.ImportItem ||
            reportTyp == ReportTypeImport.ImportTaxLine
        )
        val hasImportDecision = decision.toString.toLowerCase == "import"
        val hasExportTypes    = reportTypes.contains(ReportTypeImport.ExportItem)

        !((hasExportDecision && hasImportTypes) || (hasImportDecision && hasExportTypes))
      case None           => true
    }
  }

  private def validateThirdPartyAccess(userAnswers: UserAnswers): Boolean = {
    val isThirdParty = userAnswers.get(SelectThirdPartyEoriPage).isDefined
    if (isThirdParty) {
      val eoriRoles       = userAnswers.get(EoriRolePage).getOrElse(Set.empty)
      val reportDateRange = userAnswers.get(ReportDateRangePage)
      !(eoriRoles.contains(EoriRole.Declarant) || reportDateRange.contains(ReportDateRange.LastFullCalendarMonth))
    } else {
      true
    }
  }
}
