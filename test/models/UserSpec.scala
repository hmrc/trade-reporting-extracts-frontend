/*
 * Copyright 2026 HM Revenue & Customs
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

import models.AccessType.EXPORTS
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.*

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import scala.util.{Failure, Success}

class UserSpec extends AnyFreeSpec {

  "User JSON Format" - {
    "should serialize and deserialize a User object correctly" in {
      val user = User(
        eori = "EORI12345",
        additionalEmails = Seq("test@example.com"),
        authorisedUsers = Seq(
          AuthorisedUser(
            eori = "eori1",
            accessStart = Instant.now(),
            accessEnd = None,
            reportDataStart = None,
            reportDataEnd = None,
            accessType = Set(EXPORTS),
            referenceName = Some("someCompany")
          )
        )
      )

      val json: JsValue = Json.toJson(user)

      assert((json \ "eori").as[String] == "EORI12345")
      assert((json \ "additionalEmails").as[Seq[String]] == Seq("test@example.com"))
      assert((json \ "authorisedUsers").as[Seq[JsObject]].nonEmpty)

      // Deserialize the JSON back to the User object
      val deserializedUser: JsResult[User] = json.validate[User]
      deserializedUser match {
        case JsSuccess(deserialized, _) =>
          assert(deserialized.eori == user.eori)
          assert(deserialized.additionalEmails == user.additionalEmails)
          assert(deserialized.authorisedUsers.size == user.authorisedUsers.size)
        case JsError(errors)            =>
          fail(s"Deserialization failed with errors: $errors")
      }
    }
  }

  "AuthorisedUser JSON Format" - {
    "should serialize and deserialize an AuthorisedUser object correctly" in {
      val timeAsInstant = LocalDate
        .of(2024, 1, 1)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant

      val authorisedUser = AuthorisedUser(
        eori = "eori1",
        accessStart = timeAsInstant,
        accessEnd = None,
        reportDataStart = None,
        reportDataEnd = None,
        accessType = Set(EXPORTS),
        referenceName = Some("someCompany")
      )

      val json: JsValue = Json.toJson(authorisedUser)

      assert((json \ "eori").as[String] == "eori1")
      assert((json \ "accessType").as[Seq[String]] == Seq("EXPORTS"))
      assert((json \ "referenceName").as[String] == "someCompany")

      val deserializedUser: JsResult[AuthorisedUser] = json.validate[AuthorisedUser]
      deserializedUser match {
        case JsSuccess(deserialized, _) =>
          assert(deserialized.eori == authorisedUser.eori)
          assert(deserialized.accessType == authorisedUser.accessType)
          assert(deserialized.accessStart == timeAsInstant)
        case JsError(errors)            =>
          fail(s"Deserialization failed with errors: $errors")
      }
    }
  }
}
