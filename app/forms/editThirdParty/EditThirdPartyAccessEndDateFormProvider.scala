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

package forms.editThirdParty

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.optional
import play.api.i18n.Messages
import utils.DateTimeFormats.dateTimeFormat

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class EditThirdPartyAccessEndDateFormProvider @Inject (clock: Clock = Clock.systemUTC()) extends Mappings {

  private def dynamicMinimum(startDate: LocalDate)(implicit messages: Messages): (LocalDate, String) = {
    val today   = LocalDate.now(clock)
    val minimum = if (startDate.isAfter(today)) startDate.plusDays(1) else today
    (minimum, minimum.format(dateTimeFormat()(messages.lang)))
  }

  def apply(startDate: LocalDate)(implicit messages: Messages): Form[Option[LocalDate]] = {
    val (minDateValue, minDateArg) = dynamicMinimum(startDate)
    Form(
      "value" -> optional(
        localDate(
          invalidKey = "editThirdPartyAccessEndDate.error.invalid",
          allRequiredKey = "editThirdPartyAccessEndDate.error.required.all",
          twoRequiredKey = "editThirdPartyAccessEndDate.error.required.two",
          requiredKey = "editThirdPartyAccessEndDate.error.required"
        ).verifying(
          minDate(
            minDateValue,
            "editThirdPartyAccessEndDate.error.min",
            minDateArg
          )
        )
      )
    )
  }
}
