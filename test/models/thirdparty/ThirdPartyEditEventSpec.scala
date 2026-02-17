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
import play.api.libs.json.Json

class ThirdPartyEditEventSpec extends SpecBase {

  "DataUpdate JSON format" - {
    "must serialize and deserialize preserving values" in {
      val event =
        DataUpdate(
          dataUpdated = "accessType",
          previous = "import",
          `new` = "export"
        )

      val json   = Json.toJson(event)
      val parsed = json.validate[DataUpdate].asOpt
      parsed mustBe Some(event)
    }

    "must round trip" in {
      val json = Json.obj(
        "dataUpdated" -> "accessType",
        "previous"    -> "import",
        "new"         -> "export"
      )

      val parsed = json.validate[DataUpdate].get
      parsed.dataUpdated mustBe "accessType"
      parsed.previous mustBe "import"
      parsed.`new` mustBe "export"
    }
  }

  "ThirdPartyUpdatedEvent JSON format" - {
    "must serialize and deserialize preserving values" in {
      val event =
        ThirdPartyUpdatedEvent(
          requesterEori = "GB123",
          thirdPartyEori = "GB999",
          updatesToThirdPartyData = List(
            DataUpdate(
              dataUpdated = "accessType",
              previous = "import",
              `new` = "export"
            )
          )
        )

      val json   = Json.toJson(event)
      val parsed = json.validate[ThirdPartyUpdatedEvent].asOpt
      parsed mustBe Some(event)
    }

    "must round trip" in {
      val json = Json.obj(
        "requesterEori"           -> "GB123",
        "thirdPartyEori"          -> "GB999",
        "updatesToThirdPartyData" -> List(DataUpdate("accessType", "import", "export"))
      )

      val parsed = json.validate[ThirdPartyUpdatedEvent].get
      parsed.requesterEori mustBe "GB123"
      parsed.thirdPartyEori mustBe "GB999"
      parsed.updatesToThirdPartyData mustBe List(DataUpdate("accessType", "import", "export"))
    }
  }
}
