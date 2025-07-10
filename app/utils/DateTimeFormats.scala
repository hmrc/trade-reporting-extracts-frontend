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

import play.api.i18n.Lang

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateTimeFormats {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  private val localisedDateTimeFormatters = Map(
    "en" -> dateTimeFormatter,
    "cy" -> dateTimeFormatter.withLocale(new Locale("cy"))
  )

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedDateTimeFormatters.getOrElse(lang.code, dateTimeFormatter)

  val dateTimeHintFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d M yyyy")

  def lastFullCalendarMonth(currentDate: LocalDate): (LocalDate, LocalDate) = {
    val lastDayPrevMonth                             = currentDate.minusMonths(1).`with`(TemporalAdjusters.lastDayOfMonth())
    val tMinus2                                      = currentDate.minusDays(2)
    val (startDate, endDate): (LocalDate, LocalDate) =
      if (!lastDayPrevMonth.isBefore(tMinus2)) {
        val start = currentDate.minusMonths(2).withDayOfMonth(1)
        val end   = currentDate.minusMonths(2).`with`(TemporalAdjusters.lastDayOfMonth())
        (start, end)
      } else {
        val start = currentDate.minusMonths(1).withDayOfMonth(1)
        val end   = lastDayPrevMonth
        (start, end)
      }
    (startDate, endDate)
  }
}
