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

package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, Json, Reads, Writes, JsPath}

import java.time.Instant
import scala.reflect.ClassTag

case class UserDetails(
  eori: String,
  additionalEmails: Seq[String],
  authorisedUsers: Seq[AuthorisedUser],
  companyInformation: CompanyInformation,
  notificationEmail: NotificationEmail
)

object UserDetails:
  given format: Format[UserDetails] = Json.format[UserDetails]

val mongoInstantReads: Reads[Instant] =
  (JsPath \ "$date" \ "$numberLong").read[String].map(ms => Instant.ofEpochMilli(ms.toLong))

implicit val authorisedUserReads: Reads[AuthorisedUser] = (
  (JsPath \ "eori").read[String] and
    (JsPath \ "accessStart").read[Instant](mongoInstantReads) and
    (JsPath \ "accessEnd").readNullable[Instant](mongoInstantReads) and
    (JsPath \ "reportDataStart").readNullable[Instant](mongoInstantReads) and
    (JsPath \ "reportDataEnd").readNullable[Instant](mongoInstantReads) and
    (JsPath \ "accessType").read[Set[AccessType]] and
    (JsPath \ "referenceName").readNullable[String]
  )(AuthorisedUser.apply _)