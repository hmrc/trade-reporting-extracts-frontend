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

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Format, __}

case class AvailableReportsViewModel(
  availableUserReports: Option[Seq[AvailableUserReportsViewModel]],
  availableThirdPartyReports: Option[Seq[AvailableThirdPartyReportsViewModel]]
)

object AvailableReportsViewModel {
  implicit lazy val format: Format[AvailableReportsViewModel] = (
    (__ \ "availableUserReports").formatNullable[Seq[AvailableUserReportsViewModel]] and
      (__ \ "availableThirdPartyReports").formatNullable[Seq[AvailableThirdPartyReportsViewModel]]
  )(AvailableReportsViewModel.apply, o => Tuple.fromProductTyped(o))
}
