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

package models.audit

import play.api.libs.functional.syntax.*
import play.api.libs.json.{OWrites, __}

import java.time.Instant

final case class UserLoginEvent(
  eori: String,
  userId: String,
  affinityGroup: String,
  credentialRole: String,
  isSuccessful: Boolean,
  processedAt: Instant = Instant.now()
) extends AuditEvent {
  override def auditType: String = "UserLoginEvent"
}

object UserLoginEvent {
  given OWrites[UserLoginEvent] = (
    (__ \ "eori").write[String] and
      (__ \ "userId").write[String] and
      (__ \ "affinityGroup").write[String] and
      (__ \ "credentialRole").write[String] and
      (__ \ "outcome" \ "isSuccessful").write[Boolean] and
      (__ \ "outcome" \ "processedAt").write[Instant]
  )(o => (o.eori, o.userId, o.affinityGroup, o.credentialRole, o.isSuccessful, o.processedAt))
}
