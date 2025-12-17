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
import utils.Constants.maxReportRequestDays
import utils.DateTimeFormats
import utils.DateTimeFormats.dateTimeFormat

import java.time.{LocalDate, ZoneOffset}

class CustomRequestEndDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages    = stubMessages()
  private val currentDate: LocalDate         = LocalDate.now(ZoneOffset.UTC)
  private val startDate: LocalDate           = currentDate.minusYears(1)
  private val futureDate: LocalDate          = currentDate.plusDays(1)
  private val currentDateMinusDay: LocalDate = currentDate.minusDays(1)
  private val beforeStartDate: LocalDate     = startDate.minusDays(1)
  private val illegalReportLengthDate        = startDate.plusDays(maxReportRequestDays + 1)
  private val form                           = new CustomRequestEndDateFormProvider()(startDate, false, None)

  ".value" - {

    val validData = datesBetween(
      min = startDate,
      max = startDate.plusDays(maxReportRequestDays)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "customRequestEndDate.error.required.all")

    "not bind dates after current date" in {
      val result = form.bind(
        Map(
          "value.day"   -> futureDate.getDayOfMonth.toString,
          "value.month" -> futureDate.getMonthValue.toString,
          "value.year"  -> futureDate.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.error.afterToday",
        List(currentDate.minusDays(3).format(dateTimeFormat()(messages.lang)))
      )
    }

    "not bind dates after current date - 2 days" in {
      val result = form.bind(
        Map(
          "value.day"   -> currentDateMinusDay.getDayOfMonth.toString,
          "value.month" -> currentDateMinusDay.getMonthValue.toString,
          "value.year"  -> currentDateMinusDay.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.error.afterToday",
        List(currentDate.minusDays(3).format(dateTimeFormat()(messages.lang)))
      )
    }

    "not bind dates more than 31 days after startDate" in {
      val result = form.bind(
        Map(
          "value.day"   -> illegalReportLengthDate.getDayOfMonth.toString,
          "value.month" -> illegalReportLengthDate.getMonthValue.toString,
          "value.year"  -> illegalReportLengthDate.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.error.lengthGreaterThan31Days",
        List()
      )
    }

    "not bind dates before the startDate" in {
      val result = form.bind(
        Map(
          "value.day"   -> beforeStartDate.getDayOfMonth.toString,
          "value.month" -> beforeStartDate.getMonthValue.toString,
          "value.year"  -> beforeStartDate.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.error.beforeStartDate",
        List(startDate.format(dateTimeFormat()(messages.lang)))
      )
    }

    "for third party request, not bind dates after thirdPartyDataEndDate if it is before currentDate.minusDays(3)" in {
      val thirdPartyDataEndDate  = currentDate.minusDays(10)
      val form                   = new CustomRequestEndDateFormProvider()(startDate, true, Some(thirdPartyDataEndDate))
      val afterThirdPartyEndDate = thirdPartyDataEndDate.plusDays(1)

      val result = form.bind(
        Map(
          "value.day"   -> afterThirdPartyEndDate.getDayOfMonth.toString,
          "value.month" -> afterThirdPartyEndDate.getMonthValue.toString,
          "value.year"  -> afterThirdPartyEndDate.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.thirdParty.error",
        List(thirdPartyDataEndDate.format(dateTimeFormat()(messages.lang)))
      )
    }

    "for third party request, not bind dates after currentDate.minusDays(3) if thirdPartyDataEndDate is after currentDate.minusDays(3)" in {
      val thirdPartyDataEndDate = currentDate.minusDays(1)
      val form                  = new CustomRequestEndDateFormProvider()(startDate, true, Some(thirdPartyDataEndDate))
      val afterAllowedDate      = currentDate

      val result = form.bind(
        Map(
          "value.day"   -> afterAllowedDate.getDayOfMonth.toString,
          "value.month" -> afterAllowedDate.getMonthValue.toString,
          "value.year"  -> afterAllowedDate.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.error.afterToday",
        List(currentDate.minusDays(3).format(dateTimeFormat()(messages.lang)))
      )
    }

    "for third party request, allow dates up to thirdPartyDataEndDate if it is before currentDate.minusDays(3)" in {
      val thirdPartyDataEndDate = currentDate.minusDays(10)
      val form                  = new CustomRequestEndDateFormProvider()(currentDate.minusDays(11), true, Some(thirdPartyDataEndDate))

      val result = form.bind(
        Map(
          "value.day"   -> thirdPartyDataEndDate.getDayOfMonth.toString,
          "value.month" -> thirdPartyDataEndDate.getMonthValue.toString,
          "value.year"  -> thirdPartyDataEndDate.getYear.toString
        )
      )

      result.errors mustBe empty
    }

    "for third party request, allow dates up to currentDate.minusDays(3) if thirdPartyDataEndDate is after currentDate.minusDays(3)" in {
      val thirdPartyDataEndDate = currentDate.minusDays(1)
      val allowedDate           = currentDate.minusDays(3)
      val form                  = new CustomRequestEndDateFormProvider()(currentDate.minusDays(10), true, Some(thirdPartyDataEndDate))

      val result = form.bind(
        Map(
          "value.day"   -> allowedDate.getDayOfMonth.toString,
          "value.month" -> allowedDate.getMonthValue.toString,
          "value.year"  -> allowedDate.getYear.toString
        )
      )

      result.errors mustBe empty
    }

  }
}
