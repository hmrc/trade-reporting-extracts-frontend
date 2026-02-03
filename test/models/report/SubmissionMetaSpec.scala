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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, JsValue, Json}

import java.time.{Clock, Instant, ZoneId}

class SubmissionMetaSpec extends AnyFreeSpec with Matchers {

  private val fixedInstant: Instant = Instant.parse("2025-05-05T00:00:00Z")
  private val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

  "SubmissionMeta" - {

    "must serialise to JSON" in {
      val model = SubmissionMeta(
        reportConfirmations = Seq(ReportConfirmation("reportName", "importItem", "RE0000001")),
        notificationEmail = "test@example.com",
        submittedAt = Instant.now(fixedClock),
        isMoreThanOneReport = true
      )

      val json: JsValue = Json.toJson(model)

      (json \ "notificationEmail").as[String] mustEqual "test@example.com"
      (json \ "submittedAt").as[String] mustEqual "2025-05-05T00:00:00Z"

      val confirmations = (json \ "reportConfirmations").as[Seq[ReportConfirmation]]
      confirmations must have size 1
      confirmations.head.reportReference mustEqual "RE0000001"
      confirmations.head.reportName mustEqual "reportName"
      confirmations.head.reportType mustEqual "importItem"
    }

    "must deserialise from JSON" in {
      val json = Json.obj(
        "reportConfirmations" -> Seq(
          Json.obj(
            "reportName"      -> "reportName",
            "reportType"      -> "importItem",
            "reportReference" -> "RE0000001"
          )
        ),
        "notificationEmail"   -> "test@example.com",
        // New field name aligned to model: submittedAt (ISO-8601)
        "submittedAt"         -> "2025-05-05T00:00:00Z",
        "isMoreThanOneReport" -> true
      )

      val result = json.validate[SubmissionMeta]
      result mustBe a[JsSuccess[_]]

      val meta = result.get
      meta.notificationEmail mustEqual "test@example.com"
      meta.submittedAt mustEqual Instant.parse("2025-05-05T00:00:00Z")

      meta.reportConfirmations must have size 1
      meta.reportConfirmations.head.reportReference mustEqual "RE0000001"
      meta.reportConfirmations.head.reportName mustEqual "reportName"
      meta.reportConfirmations.head.reportType mustEqual "importItem"

      meta.isMoreThanOneReport mustEqual true
    }

    "must handle multiple report confirmations" in {
      val json = Json.obj(
        "reportConfirmations" -> Seq(
          Json.obj(
            "reportName"      -> "importHeader",
            "reportType"      -> "importHeader",
            "reportReference" -> "RE0000002"
          ),
          Json.obj(
            "reportName"      -> "exportItem",
            "reportType"      -> "exportItem",
            "reportReference" -> "RE0000003"
          )
        ),
        "notificationEmail"   -> "test@example.com",
        "submittedAt"         -> "2025-05-05T12:34:56Z",
        "isMoreThanOneReport" -> true
      )

      val result = json.validate[SubmissionMeta]
      result mustBe a[JsSuccess[_]]

      val meta = result.get
      meta.submittedAt mustEqual Instant.parse("2025-05-05T12:34:56Z")
      meta.reportConfirmations.map(_.reportReference) must contain allOf ("RE0000002", "RE0000003")
    }
  }
}
