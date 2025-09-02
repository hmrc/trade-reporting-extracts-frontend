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

package forms.thirdparty

import forms.behaviours.DateBehaviours
import forms.thirdparty.ThirdPartyAccessStartDateFormProvider
import play.api.data.FormError
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessages
import utils.DateTimeFormats
import utils.DateTimeFormats.dateTimeFormat

import java.time.{LocalDate, ZoneOffset}

class ThirdPartyAccessStartDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form                        = new ThirdPartyAccessStartDateFormProvider()()

  val min: LocalDate = LocalDate.now
  val max: LocalDate = LocalDate.now.plusYears(1).minusDays(1)

  ".value" - {

    val validData = datesBetween(
      min = min,
      max = max
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "thirdPartyAccessStartDate.error.required.all")

    behave like dateFieldWithMax(
      form,
      "value",
      max,
      FormError(
        "value",
        "thirdPartyAccessStartDate.error.max",
        Seq(LocalDate.now.plusYears(1).format(dateTimeFormat()(Lang("en"))))
      )
    )

    behave like dateFieldWithMin(
      form,
      "value",
      min,
      FormError("value", "thirdPartyAccessStartDate.error.min", Seq())
    )
  }
}
