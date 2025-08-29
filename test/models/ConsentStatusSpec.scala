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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class ConsentStatusSpec extends AnyWordSpec with Matchers {

  "ConsentStatus" should {

    "serialize to JSON correctly" in {
      Json.toJson(ConsentStatus.Granted.toString) mustEqual JsString("1")
      Json.toJson(ConsentStatus.Denied.toString) mustEqual JsString("0")
    }

    "deserialize from valid JSON strings" in {
      JsString("1").validate[ConsentStatus] mustEqual JsSuccess(ConsentStatus.Granted)
      JsString("0").validate[ConsentStatus] mustEqual JsSuccess(ConsentStatus.Denied)
    }

    "deserialize from empty string as Denied" in {
      JsString("").validate[ConsentStatus] mustEqual JsSuccess(ConsentStatus.Denied)
    }

    "deserialize from null as Denied" in {
      JsNull.validate[ConsentStatus] mustEqual JsSuccess(ConsentStatus.Denied)
    }

    "fail to deserialize from invalid type" in {
      JsNumber(1).validate[ConsentStatus].isError mustBe true
      JsBoolean(true).validate[ConsentStatus].isError mustBe true
    }

    "convert from string using fromString method" in {
      ConsentStatus.fromString("1") mustEqual ConsentStatus.Granted
      ConsentStatus.fromString("0") mustEqual ConsentStatus.Denied
      ConsentStatus.fromString("") mustEqual ConsentStatus.Denied
      ConsentStatus.fromString("unexpected") mustEqual ConsentStatus.Denied
    }

    "convert to string using toString method" in {
      ConsentStatus.toString(ConsentStatus.Granted) mustEqual "1"
      ConsentStatus.toString(ConsentStatus.Denied) mustEqual "0"
    }
  }
}
