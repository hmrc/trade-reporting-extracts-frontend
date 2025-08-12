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

package forms.report

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{LocalDate, ZoneOffset}

class CustomRequestStartDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form                        = new CustomRequestStartDateFormProvider()()

  val min: LocalDate = LocalDate.now(ZoneOffset.UTC).minusYears(4)
  val max: LocalDate = LocalDate.now(ZoneOffset.UTC)

  ".value" - {

    val validData = datesBetween(
      min = min,
      max = max
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "customRequestStartDate.error.required.all")

    behave like dateFieldWithMax(
      form,
      "value",
      max,
      FormError("value", "customRequestStartDate.error.max", Seq())
    )

    behave like dateFieldWithMin(
      form,
      "value",
      min,
      FormError("value", "customRequestStartDate.error.min", Seq())
    )

  }
}
