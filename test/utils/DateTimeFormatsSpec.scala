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

import models.{ThirdPartyDetails, UserActiveStatus}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessages
import utils.DateTimeFormats.dateTimeFormat

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  val fixedInstant: Instant       = Instant.parse("2025-05-05T00:00:00Z")
  val fixedClock: Clock           = Clock.fixed(fixedInstant, ZoneOffset.UTC)
  implicit val messages: Messages = stubMessages()
  private val fmt                 = DateTimeFormats.dateTimeFormat()(messages.lang)

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
      result must include("AM")
    }

    "must return the current time formatted in Welsh" in {
      val lang   = Lang("cy")
      val result = DateTimeFormats.formattedSystemTime(fixedClock)(lang)
      result must include("yb")
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
  "computeCalculatedDateValue(accessStart, accessEnd, dataStart, dataEnd)" - {

    "return dataStart + 2 days (formatted) when accessEnd and dataEnd are empty" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val dataStart   = LocalDate.of(2025, 11, 1)

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = None,
        dataStart = Some(dataStart),
        dataEnd = None
      )

      result mustBe Some(dataStart.plusDays(2).format(fmt))
    }

    "return accessStart (formatted) when dataStart is at least 3 days before accessStart" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val dataStart   = LocalDate.of(2025, 10, 29)

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = None,
        dataStart = Some(dataStart),
        dataEnd = None
      )

      result mustBe Some(accessStart.format(fmt))
    }

    "return None when data range difference is < 3 days" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val dataStart   = Some(LocalDate.of(2025, 11, 1))
      val dataEnd     = Some(LocalDate.of(2025, 11, 2))

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = None,
        dataStart = dataStart,
        dataEnd = dataEnd
      )

      result mustBe None
    }

    "return active date when all provided and both ranges are > 3 days" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val accessEnd   = Some(LocalDate.of(2025, 11, 10)) // > 3 days
      val dataStart   = Some(LocalDate.of(2025, 11, 1))
      val dataEnd     = Some(LocalDate.of(2025, 11, 10)) // > 3 days

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = accessEnd,
        dataStart = dataStart,
        dataEnd = dataEnd
      )

      result mustBe Some(LocalDate.of(2025, 11, 3).format(fmt)) // dataStart + 2
    }

    "return None when all provided but data range is not > 3 days" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val accessEnd   = Some(LocalDate.of(2025, 11, 10)) // > 3 days
      val dataStart   = Some(LocalDate.of(2025, 11, 1))
      val dataEnd     = Some(LocalDate.of(2025, 11, 3)) // 2 days, not > 3

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = accessEnd,
        dataStart = dataStart,
        dataEnd = dataEnd
      )

      result mustBe None
    }

    "return accessStart when only accessEnd provided and access range is > 3 days" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val accessEnd   = Some(LocalDate.of(2025, 11, 10)) // > 3 days

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = accessEnd,
        dataStart = None,
        dataEnd = None
      )

      result mustBe Some(accessStart.format(fmt))
    }

    "return None when only accessEnd provided and access range is not > 3 days" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val accessEnd   = Some(LocalDate.of(2025, 11, 3)) // 2 days

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = accessEnd,
        dataStart = None,
        dataEnd = None
      )

      result mustBe None
    }

    "return active date when only dataStart and dataEnd provided and data range is > 3 days" in {
      val accessStart = LocalDate.of(2025, 11, 1)
      val dataStart   = Some(LocalDate.of(2025, 11, 1))
      val dataEnd     = Some(LocalDate.of(2025, 11, 6)) // 5 days

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = None,
        dataStart = dataStart,
        dataEnd = dataEnd
      )

      result mustBe Some(LocalDate.of(2025, 11, 3).format(fmt)) // dataStart + 2
    }

    "default to accessStart when all are empty" in {
      val accessStart = LocalDate.of(2025, 11, 1)

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = None,
        dataStart = None,
        dataEnd = None
      )

      result mustBe Some(accessStart.format(fmt))
    }

    "default to accessStart when accessEnd and dataEnd provided but dataStart missing" in {
      val accessStart = LocalDate.of(2025, 11, 1)

      val result = DateTimeFormats.computeCalculatedDateValue(
        accessStart = accessStart,
        accessEnd = Some(LocalDate.of(2025, 11, 10)),
        dataStart = None,
        dataEnd = Some(LocalDate.of(2025, 11, 15))
      )

      result mustBe Some(accessStart.format(fmt))
    }
  }

  "computeCalculatedDateValue(details, status)" - {

    "return None when status is Active (short-circuits without using details)" in {
      val result = DateTimeFormats.computeCalculatedDateValue(
        details = null.asInstanceOf[ThirdPartyDetails],
        status = UserActiveStatus.Active
      )
      result mustBe None
    }
  }
}
