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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.dateTimeFormat

import java.time.{Clock, Instant, LocalDate, ZoneId, ZoneOffset}

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  val fixedInstant: Instant = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.systemDefault())

  ".dateTimeFormat" - {

    "must format dates in English" in {
      val formatter = dateTimeFormat()(Lang("en"))
      val result    = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }

    "must format dates in Welsh" in {
      val formatter = dateTimeFormat()(Lang("cy"))
      val result    = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Ionawr 2023"
    }

    "must default to English format" in {
      val formatter = dateTimeFormat()(Lang("de"))
      val result    = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }
  }

  "formattedTime" - {
    "must return the current time in english" in {
      val lang   = Lang("en")
      val result = DateTimeFormats.formattedSystemTime(fixedClock)(lang)
      result mustBe "12:00 AM"
    }

    "must return the current time formatted in Welsh" in {
      val lang   = Lang("cy")
      val result = DateTimeFormats.formattedSystemTime(fixedClock)(lang)
      result mustBe "12:00 yb"
    }
  }

  "lastFullCalendarMonth" - {

    "must return the correct dates when the last day of the previous month is after current date minus 2 days, within 1 day" in {
      val currentDate          = LocalDate.of(2025, 7, 1)
      val (startDate, endDate) = DateTimeFormats.lastFullCalendarMonth(currentDate)
      startDate mustEqual LocalDate.of(2025, 5, 1)
      endDate mustEqual LocalDate.of(2025, 5, 31)
    }

    "must return the correct dates when the last day of the previous month is after current date minus 2 days, with 2 days" in {
      val currentDate          = LocalDate.of(2025, 7, 2)
      val (startDate, endDate) = DateTimeFormats.lastFullCalendarMonth(currentDate)
      startDate mustEqual LocalDate.of(2025, 5, 1)
      endDate mustEqual LocalDate.of(2025, 5, 31)
    }

    "must return the correct start and end dates for the last full calendar month" in {
      val currentDate          = LocalDate.of(2025, 7, 3)
      val (startDate, endDate) = DateTimeFormats.lastFullCalendarMonth(currentDate)
      startDate mustEqual LocalDate.of(2025, 6, 1)
      endDate mustEqual LocalDate.of(2025, 6, 30)
    }

    "must return the correct dates in a leap year when the last day of the previous is after current date minus 2 days" in {
      val currentDate          = LocalDate.of(2024, 3, 1)
      val (startDate, endDate) = DateTimeFormats.lastFullCalendarMonth(currentDate)
      startDate mustEqual LocalDate.of(2024, 1, 1)
      endDate mustEqual LocalDate.of(2024, 1, 31)
    }

    "must return the correct dates in a leap year when the last day of the previous is not after current date minus 2 days" in {
      val currentDate          = LocalDate.of(2024, 3, 3)
      val (startDate, endDate) = DateTimeFormats.lastFullCalendarMonth(currentDate)
      startDate mustEqual LocalDate.of(2024, 2, 1)
      endDate mustEqual LocalDate.of(2024, 2, 29)
    }
  }
}
