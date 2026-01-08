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

package models.thirdparty

import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, JsValue, Json}

import java.time.Instant

class ThirdPartyRemovalMetaSpec extends AnyFreeSpec with Matchers {

  "ThirdPartyRemovalMeta" - {

    "must serialise to JSON" in {
      val submitted = Instant.parse("2025-11-06T12:34:56Z")

      val model = ThirdPartyRemovalMeta(
        eori = "GB123456789000",
        submittedAt = submitted,
        notificationEmail = Some("notify@test.example")
      )

      val json: JsValue = Json.toJson(model)

      (json \ "eori").as[String] mustEqual "GB123456789000"
      (json \ "submittedAt").as[String] mustEqual submitted.toString
      (json \ "notificationEmail").asOpt[String].value mustEqual "notify@test.example"
    }

    "must deserialise from JSON" in {
      val json = Json.obj(
        "eori"              -> "GB123456789000",
        "submittedAt"       -> "2025-11-06T12:34:56Z",
        "notificationEmail" -> "notify@test.example"
      )

      val result = json.validate[ThirdPartyRemovalMeta]
      result mustBe a[JsSuccess[_]]

      val model = result.get
      model.eori mustEqual "GB123456789000"
      model.submittedAt mustEqual Instant.parse("2025-11-06T12:34:56Z")
      model.notificationEmail.value mustEqual "notify@test.example"
    }

    "must handle missing optional notificationEmail" in {
      val json = Json.obj(
        "eori"        -> "GB123456789000",
        "submittedAt" -> "2025-11-06T12:34:56Z"
      )

      val result = json.validate[ThirdPartyRemovalMeta]
      result mustBe a[JsSuccess[_]]

      val model = result.get
      model.notificationEmail mustBe None
      model.eori mustEqual "GB123456789000"
      model.submittedAt mustEqual Instant.parse("2025-11-06T12:34:56Z")
    }

    "must fail to deserialise when required fields are missing" in {
      val jsonMissingEori = Json.obj(
        "submittedAt" -> "2025-11-06T12:34:56Z"
      )

      val result1 = jsonMissingEori.validate[ThirdPartyRemovalMeta]
      result1.isError mustBe true

      val jsonMissingSubmittedAt = Json.obj(
        "eori" -> "GB123456789000"
      )

      val result2 = jsonMissingSubmittedAt.validate[ThirdPartyRemovalMeta]
      result2.isError mustBe true
    }
  }
}
