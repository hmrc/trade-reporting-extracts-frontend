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
import play.api.data.validation.Constraint
import play.api.i18n.Messages
import utils.DateTimeFormats
import utils.DateTimeFormats.dateTimeFormat

import java.time.{LocalDate, ZoneOffset}
import javax.inject.Inject

class CustomRequestEndDateFormProvider @Inject() extends Mappings {

  private val currentDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

  def apply(startDate: LocalDate, maybeThirdPartyRequest: Boolean, thirdPartyDataEndDate: Option[LocalDate])(implicit
    messages: Messages
  ): Form[LocalDate] = {
    val maxReportLength = startDate.plusDays(30)

    Form(
      "value" -> localDate(
        invalidKey = "customRequestEndDate.error.invalid",
        allRequiredKey = "customRequestEndDate.error.required.all",
        twoRequiredKey = "customRequestEndDate.error.required.two",
        requiredKey = "customRequestEndDate.error.required"
      ).verifying(
        firstError(
          determineMaximumDate(maybeThirdPartyRequest, thirdPartyDataEndDate, startDate)(messages),
          maxDate(maxReportLength, "customRequestEndDate.error.lengthGreaterThan31Days")
        )
      ).verifying(
        minDate(
          startDate,
          "customRequestEndDate.error.beforeStartDate",
          startDate.format(dateTimeFormat()(messages.lang))
        )
      )
    )
  }

  private def determineMaximumDate(
    maybeThirdPartyRequest: Boolean,
    thirdPartyDataEndDate: Option[LocalDate],
    startDate: LocalDate
  )(messages: Messages): Constraint[LocalDate] =
    (maybeThirdPartyRequest, thirdPartyDataEndDate, startDate) match {
      case (true, Some(_), _) =>
        if (thirdPartyDataEndDate.get.isBefore(currentDate.minusDays(3))) {
          maxDate(
            thirdPartyDataEndDate.get,
            "customRequestEndDate.thirdParty.error",
            thirdPartyDataEndDate.get.format(dateTimeFormat()(messages.lang))
          )
        } else {
          maxDate(
            currentDate.minusDays(3),
            "customRequestEndDate.error.afterToday",
            currentDate.minusDays(3).format(dateTimeFormat()(messages.lang))
          )
        }
      case (_, _, _)          =>
        maxDate(
          currentDate.minusDays(3),
          "customRequestEndDate.error.afterToday",
          currentDate.minusDays(3).format(dateTimeFormat()(messages.lang))
        )
    }

}
