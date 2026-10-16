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

package forms.mappings

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class LocalDateFormatterSpec extends AnyFreeSpec with Matchers {

  private implicit val messages: Messages = stubMessages()

  private val preservingFormatter = new LocalDateFormatter(
    invalidKey = "error.invalid",
    allRequiredKey = "error.required.all",
    twoRequiredKey = "error.required.two",
    requiredKey = "error.required",
    preserveInvalidFieldKey = true
  )

  private val defaultFormatter = new LocalDateFormatter(
    invalidKey = "error.invalid",
    allRequiredKey = "error.required.all",
    twoRequiredKey = "error.required.two",
    requiredKey = "error.required"
  )

  "LocalDateFormatter" - {

    "preserve errors for each invalid component" in {
      val result = preservingFormatter.bind(
        "value",
        Map(
          "value.day"   -> "a",
          "value.month" -> "13",
          "value.year"  -> "b"
        )
      )

      result mustBe Left(
        Seq(
          FormError("value.day", "error.invalid"),
          FormError("value.month", "error.invalid"),
          FormError("value.year", "error.invalid")
        )
      )
    }

    "mark an out-of-range day even when the month is also invalid" in {
      val result = preservingFormatter.bind(
        "value",
        Map(
          "value.day"   -> "123",
          "value.month" -> "123",
          "value.year"  -> "123"
        )
      )

      result mustBe Left(
        Seq(
          FormError("value.day", "error.invalid"),
          FormError("value.month", "error.invalid")
        )
      )
    }

    "report an impossible date against the day component when configured" in {
      val result = preservingFormatter.bind(
        "value",
        Map(
          "value.day"   -> "31",
          "value.month" -> "2",
          "value.year"  -> "2025"
        )
      )

      result mustBe Left(Seq(FormError("value.day", "error.invalid")))
    }

    "keep errors on the date field by default" in {
      val result = defaultFormatter.bind(
        "value",
        Map(
          "value.day"   -> "a",
          "value.month" -> "2",
          "value.year"  -> "2025"
        )
      )

      result mustBe Left(Seq(FormError("value", "error.invalid")))
    }
  }
}
