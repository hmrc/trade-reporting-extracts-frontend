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
import utils.DateTimeFormats.dateTimeFormat

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class CustomRequestEndDateFormProviderSpec extends DateBehaviours {

  private val fixedToday        = LocalDate.of(2026, 2, 1)
  private val fixedInstant      = fixedToday.atStartOfDay().toInstant(ZoneOffset.UTC)
  private val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  private implicit val messages: Messages = stubMessages()

  private val currentDate: LocalDate         = LocalDate.now(fixedClock)
  private val startDate: LocalDate           = currentDate.minusDays(1)
  private val futureDate: LocalDate          = currentDate.plusDays(1)
  private val currentDateMinusDay: LocalDate = currentDate.minusDays(1)
  private val illegalReportLengthDate        = startDate.plusDays(maxReportRequestDays + 1)

  private def mkForm(start: LocalDate, isThirdParty: Boolean, tpEnd: Option[LocalDate]) =
    new CustomRequestEndDateFormProvider(fixedClock)(start, isThirdParty, tpEnd)

  ".value (valid window)" - {

    val startWithWindow: LocalDate = currentDate.minusDays(10)
    val formWithWindow             = mkForm(startWithWindow, isThirdParty = false, None)

    val capBy31Days      = startWithWindow.plusDays(maxReportRequestDays)
    val capByToday3      = currentDate.minusDays(3)
    val upper: LocalDate = if (capBy31Days.isBefore(capByToday3)) capBy31Days else capByToday3

    val validData = datesBetween(
      min = startWithWindow,
      max = upper
    )

    behave like dateField(formWithWindow, "value", validData)

    behave like mandatoryDateField(formWithWindow, "value", "customRequestEndDate.error.required.all")
  }

  ".value (error cases with base form)" - {
    val form = mkForm(startDate, isThirdParty = false, None)

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

    "not bind dates after current date - 1 day" in {
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
        Nil
      )
    }

    // IMPORTANT: Because max constraints run first via firstError(...), ensure the chosen date
    // passes the "today - 3" cap so the minDate(startDate) failure is surfaced.
    "not bind dates before the startDate (ensure max cap passes so min triggers)" in {
      val dateAtCap = currentDate.minusDays(3) // <= cap; still before startDate (today - 1)
      val result    = form.bind(
        Map(
          "value.day"   -> dateAtCap.getDayOfMonth.toString,
          "value.month" -> dateAtCap.getMonthValue.toString,
          "value.year"  -> dateAtCap.getYear.toString
        )
      )

      result.errors must contain only FormError(
        "value",
        "customRequestEndDate.error.beforeStartDate",
        List(startDate.format(dateTimeFormat()(messages.lang)))
      )
    }
  }

  ".value (third-party cases)" - {

    "for third party request, not bind dates after thirdPartyDataEndDate if it is before currentDate.minusDays(3)" in {

      val thirdPartyDataEndDate = currentDate.minusDays(10)
      val localStartDate        = currentDate.minusDays(20)

      val form = mkForm(localStartDate, isThirdParty = true, Some(thirdPartyDataEndDate))

      val afterThirdPartyEndDate = thirdPartyDataEndDate.plusDays(1) // currentDate - 9

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
      val form                  = mkForm(startDate, isThirdParty = true, Some(thirdPartyDataEndDate))
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
      val form                  = mkForm(currentDate.minusDays(11), isThirdParty = true, Some(thirdPartyDataEndDate))

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
      val form                  = mkForm(currentDate.minusDays(10), isThirdParty = true, Some(thirdPartyDataEndDate))

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
