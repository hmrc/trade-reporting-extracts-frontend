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
import utils.DateTimeFormats
import utils.DateTimeFormats.dateTimeFormat

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class CustomRequestEndDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages    = stubMessages()
  private val currentDate: LocalDate         = LocalDate.now(ZoneOffset.UTC)
  private val startDate: LocalDate           = currentDate.minusYears(1)
  private val futureDate: LocalDate          = currentDate.plusDays(1)
  private val currentDateMinusDay: LocalDate = currentDate.minusDays(1)
  private val beforeStartDate: LocalDate     = startDate.minusDays(1)
  private val illegalReportLengthDate        = startDate.plusDays(32)
  private val maxReportLength: LocalDate     = startDate.plusDays(31)
  private val form                           = new CustomRequestEndDateFormProvider()(startDate)

  ".value" - {

    val validData = datesBetween(
      min = startDate,
      max = startDate.plusDays(31)
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

  }
}
