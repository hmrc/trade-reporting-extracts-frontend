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

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Format, Json, OFormat, __}

case class AvailableThirdPartyReportsViewModel(
  reportName: String,
  referenceNumber: String,
  companyName: String,
  reportType: String,
  expiryDate: String,
  action: String
)

object AvailableThirdPartyReportsViewModel {
  implicit val format: OFormat[AvailableThirdPartyReportsViewModel] = Json.format[AvailableThirdPartyReportsViewModel]
}
