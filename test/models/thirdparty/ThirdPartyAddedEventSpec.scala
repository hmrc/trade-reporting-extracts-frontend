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

package models.thirdparty

import base.SpecBase
import play.api.libs.json.{JsObject, Json}

class ThirdPartyAddedEventSpec extends SpecBase {

  "ThirdPartyAddedEvent JSON format" - {
    "must serialize and deserialize preserving values" in {
      val event = ThirdPartyAddedEvent(
        IsImporterExporterForDataToShare = true,
        thirdPartyEoriAccessGiven = false,
        thirdPartyGivenAccessAllData = true,
        requesterEori = "GB123",
        thirdPartyEori = "GB999",
        thirdPartyBusinessInformation = Some("Business Info"),
        thirdPartyReferenceName = Some("Ref Name"),
        thirdPartyAccessStart = "2025-01-01",
        thirdPartyAccessEnd = "2025-12-31",
        dataAccessType = "import",
        thirdPartyDataStart = "2025-01-01",
        thirdPartyDataEnd = "2025-12-31"
      )

      val json   = Json.toJson(event)
      val parsed = json.validate[ThirdPartyAddedEvent].asOpt
      parsed mustBe Some(event)
    }

    "must handle missing optional fields" in {
      val json = Json.obj(
        "IsImporterExporterForDataToShare" -> false,
        "thirdPartyEoriAccessGiven"        -> true,
        "thirdPartyGivenAccessAllData"     -> false,
        "requesterEori"                    -> "R1",
        "thirdPartyEori"                   -> "T1",
        "thirdPartyAccessStart"            -> "2025-01-01",
        "thirdPartyAccessEnd"              -> "2025-12-31",
        "dataAccessType"                   -> "import",
        "thirdPartyDataStart"              -> "2025-01-01",
        "thirdPartyDataEnd"                -> "2025-12-31"
      )

      val parsed = json.validate[ThirdPartyAddedEvent].get
      parsed.thirdPartyBusinessInformation mustBe None
      parsed.thirdPartyReferenceName mustBe None
      parsed.IsImporterExporterForDataToShare mustBe false
      parsed.thirdPartyEoriAccessGiven mustBe true
    }

    "must round trip" in {
      val event = ThirdPartyAddedEvent(
        IsImporterExporterForDataToShare = false,
        thirdPartyEoriAccessGiven = true,
        thirdPartyGivenAccessAllData = false,
        requesterEori = "REQ",
        thirdPartyEori = "TP",
        thirdPartyBusinessInformation = None,
        thirdPartyReferenceName = None,
        thirdPartyAccessStart = "2025-01-01",
        thirdPartyAccessEnd = "2025-12-31",
        dataAccessType = "import",
        thirdPartyDataStart = "2025-01-01",
        thirdPartyDataEnd = "2025-12-31"
      )

      val json = Json.toJson(event).as[JsObject]
      (json \ "IsImporterExporterForDataToShare").as[Boolean] mustBe false
      (json \ "requesterEori").as[String] mustBe "REQ"
      (json \ "thirdPartyBusinessInformation").toOption mustBe None
    }
  }

}
