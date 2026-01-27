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

package models.report

import models.{ReportStatus, ReportTypeName}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import utils.{DateTimeFormats, ReportHelpers}

import java.time.Instant

case class RequestedUserReportViewModel(
  referenceNumber: String,
  reportName: String,
  requestedDate: Instant,
  reportType: ReportTypeName,
  reportStartDate: Instant,
  reportEndDate: Instant,
  reportStatus: ReportStatus
) {
  def formattedRequestedDate(implicit messages: Messages): String =
    DateTimeFormats.shortDateFormatter(requestedDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)

  def formattedReportStartDate(implicit messages: Messages): String =
    DateTimeFormats.shortDateFormatter(reportStartDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)

  def formattedReportEndDate(implicit messages: Messages): String =
    DateTimeFormats.shortDateFormatter(reportEndDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)

  def formattedReportType: String = ReportHelpers.getReportType(reportType)
}

object RequestedUserReportViewModel {
  def formatExpiryDate(expiryDate: Instant)(implicit messages: Messages): String =
    DateTimeFormats.shortDateFormatter(expiryDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)

  implicit val format: OFormat[RequestedUserReportViewModel] = Json.format[RequestedUserReportViewModel]
}
