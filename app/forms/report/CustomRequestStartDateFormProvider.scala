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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.i18n.Messages

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class CustomRequestStartDateFormProvider @Inject (clock: Clock = Clock.systemUTC()) extends Mappings {

  private val dateFourYearsAgo: LocalDate = LocalDate.now(clock).minusYears(4)
  private val currentDate: LocalDate      = LocalDate.now(clock)

  def apply(
    maybeThirdPartyRequest: Boolean,
    thirdPartyDataStartDate: Option[LocalDate],
    thirdPartyDataEndDate: Option[LocalDate]
  )(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey = "customRequestStartDate.error.invalid",
        allRequiredKey = "customRequestStartDate.error.required.all",
        twoRequiredKey = "customRequestStartDate.error.required.two",
        requiredKey = "customRequestStartDate.error.required"
      )
        .verifying(
          firstError(
            determineMaxDate(maybeThirdPartyRequest, thirdPartyDataStartDate, thirdPartyDataEndDate),
            determineMinimumDate(maybeThirdPartyRequest, thirdPartyDataStartDate, thirdPartyDataEndDate)
          )
        )
    )

  private def determineMinimumDate(
    maybeThirdPartyRequest: Boolean,
    thirdPartyDataStartDate: Option[LocalDate],
    thirdPartyDataEndDate: Option[LocalDate]
  ) =
    (maybeThirdPartyRequest, thirdPartyDataStartDate, thirdPartyDataEndDate) match {
      case (true, Some(_), _) if thirdPartyDataStartDate.get.isAfter(dateFourYearsAgo) =>
        minDate(thirdPartyDataStartDate.get, "customRequestStartDate.thirdPartyDataRange.error")
      case (_, _, _)                                                                   =>
        minDate(
          dateFourYearsAgo,
          "customRequestStartDate.error.min"
        )
    }

  private def determineMaxDate(
    maybeThirdPartyRequest: Boolean,
    thirdPartyDataStartDate: Option[LocalDate],
    thirdPartyDataEndDate: Option[LocalDate]
  ) =
    (maybeThirdPartyRequest, thirdPartyDataStartDate, thirdPartyDataEndDate) match {
      case (false, None, None)      =>
        maxDate(currentDate.minusDays(3), "customRequestStartDate.error.max")
      case (true, Some(_), Some(_)) =>
        if (thirdPartyDataEndDate.get.isAfter(currentDate.minusDays(3))) {
          maxDate(currentDate.minusDays(3), "customRequestStartDate.error.max")
        } else {
          maxDate(thirdPartyDataEndDate.get, "customRequestStartDate.thirdPartyDataRange.error")
        }
      case (_, _, _)                => maxDate(currentDate.minusDays(3), "customRequestStartDate.error.max")
    }
}
