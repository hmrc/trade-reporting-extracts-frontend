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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import utils.DateTimeFormats
import utils.DateTimeFormats.dateTimeFormat

import java.time.LocalDate
import javax.inject.Inject

class ThirdPartyAccessStartDateFormProvider @Inject() extends Mappings {

  private val currentDate: LocalDate = LocalDate.now()

  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "thirdPartyAccessStartDate.error.invalid",
        allRequiredKey = "thirdPartyAccessStartDate.error.required.all",
        twoRequiredKey = "thirdPartyAccessStartDate.error.required.two",
        requiredKey = "thirdPartyAccessStartDate.error.required"
      )
        .verifying(
          minDate(
            currentDate,
            "thirdPartyAccessStartDate.error.min"
          )
        )
    )
}
