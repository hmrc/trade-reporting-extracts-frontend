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

package models.availableReports

import models.ReportTypeName
import play.api.libs.json.{Json, OFormat}
import utils.ReportHelpers

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

case class AvailableUserReportsViewModel(
  reportName: String,
  referenceNumber: String,
  expiryDate: Instant,
  reportType: ReportTypeName,
  action: Seq[AvailableReportAction]
) {
  def formattedExpiryDate: String =
    AvailableUserReportsViewModel.dateFormatter.format(expiryDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)
  def formattedReportType: String = AvailableUserReportsViewModel.getReportType(reportType)

}

object AvailableUserReportsViewModel {
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH)

  def formatExpiryDate(expiryDate: Instant): String =
    dateFormatter.format(expiryDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)

  def getReportType(reportType: ReportTypeName): String =
    ReportHelpers.getReportType(reportType)

  implicit val format: OFormat[AvailableUserReportsViewModel] =
    Json.format[AvailableUserReportsViewModel]
}
