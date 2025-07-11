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

import models.ReportStatus.NO_DATA_AVAILABLE
import models.{ReportStatus, UserAnswers}
import pages.report.ReportTypeImportPage
import play.twirl.api.Html

object ReportHelpers {
  def isMoreThanOneReport(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportTypeImportPage).exists(_.size > 1)

  def formatBytes(bytes: Long): String = {
    val units = List("bytes", "KB", "MB", "GB", "TB")
    if (bytes == 0L) "0.00 KB"
    else {
      val idx   = math.min((math.log(bytes) / math.log(1024)).toInt, units.size - 1)
      val value = bytes / math.pow(1024, idx)
      f"$value%.2f ${units(idx)}"
    }
  }

  def getReportType(reportType: models.ReportTypeName): String =
    Option(reportType)
      .map {
        case models.ReportTypeName.IMPORTS_HEADER_REPORT  => "Import header"
        case models.ReportTypeName.IMPORTS_ITEM_REPORT    => "Import item"
        case models.ReportTypeName.IMPORTS_TAXLINE_REPORT => "Import tax line"
        case models.ReportTypeName.EXPORTS_ITEM_REPORT    => "Export item"
      }
      .getOrElse(throw new IllegalArgumentException("Unknown or null report type"))

  def formatReportStatusKey(status: ReportStatus): String = status match {
    case ReportStatus.COMPLETE          => "requestedReports.status.complete"
    case ReportStatus.ERROR             => "requestedReports.status.error"
    case ReportStatus.IN_PROGRESS       => "requestedReports.status.inProgress"
    case ReportStatus.NO_DATA_AVAILABLE => "requestedReports.status.noDataAvailable"
  }

  def reportStatusDisplayData(status: ReportStatus): (String, String) = {
    val key      = formatReportStatusKey(status)
    val cssClass = status match {
      case ReportStatus.COMPLETE    => "govuk-tag--green"
      case ReportStatus.ERROR       => "govuk-tag--red"
      case ReportStatus.IN_PROGRESS => "govuk-tag--blue"
      case NO_DATA_AVAILABLE        => "govuk-tag--red"
    }
    (key, cssClass)
  }

}
