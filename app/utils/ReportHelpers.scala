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
import play.api.mvc.{Call, Result}
import play.api.mvc.Results.Redirect

import java.time.Instant

case class ReportStatusDisplayData(
  key: String,
  cssClass: String,
  maybeRedirect: Option[String]
)

object ReportHelpers {
  def isMoreThanOneReport(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ReportTypeImportPage).exists(_.size > 1)

  def formatBytes(bytes: Long): String = {
    val units = List("bytes", "KB", "MB", "GB", "TB")
    if bytes == 0L then "0.00 KB"
    else {
      val idx   = math.min((math.log(bytes.toDouble) / math.log(1024)).toInt, units.size - 1)
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

  def reportStatusDisplayData(
    status: ReportStatus,
    reportName: String,
    reportRef: String,
    reportStartDate: String,
    reportEndDate: String
  ): ReportStatusDisplayData = status match {
    case ReportStatus.COMPLETE          =>
      ReportStatusDisplayData("requestedReports.status.complete", "govuk-tag--green", None)
    case ReportStatus.ERROR             =>
      ReportStatusDisplayData(
        "requestedReports.status.error",
        "govuk-tag--red",
        Some(controllers.problem.routes.ReportFailedController.onPageLoad(reportName, reportRef).url)
      )
    case ReportStatus.IN_PROGRESS       =>
      ReportStatusDisplayData("requestedReports.status.inProgress", "govuk-tag--blue", None)
    case ReportStatus.NO_DATA_AVAILABLE =>
      ReportStatusDisplayData(
        "requestedReports.status.noDataAvailable",
        "govuk-tag--red",
        Some(
          controllers.problem.routes.NoDataFoundController
            .onPageLoad(reportName, reportRef, reportStartDate, reportEndDate)
            .url
        )
      )
  }
}
