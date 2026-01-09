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
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import utils.DateTimeFormats

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

case class AvailableThirdPartyReportsViewModel(
  reportName: String,
  referenceNumber: String,
  expiryDate: Instant,
  reportType: ReportTypeName,
  companyName: String,
  action: Seq[AvailableReportAction]
) {
  def formattedExpiryDate(implicit messages: Messages): String =
    DateTimeFormats.shortDateFormatter(expiryDate.atZone(java.time.ZoneOffset.UTC).toLocalDate)
  def formattedReportType: String                              = AvailableUserReportsViewModel.getReportType(reportType)
}

object AvailableThirdPartyReportsViewModel {
  implicit val format: OFormat[AvailableThirdPartyReportsViewModel] = Json.format[AvailableThirdPartyReportsViewModel]
}
