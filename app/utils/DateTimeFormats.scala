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
import play.api.i18n.{Lang, Messages}

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.{ChronoUnit, TemporalAdjusters}
import java.util.Locale
import java.time.temporal.ChronoUnit.DAYS

object DateTimeFormats {

  private val fullPattern  = "d MMMM yyyy"
  private val shortPattern = "d MMM yyyy"

  private def localisedFormatters(pattern: String): Map[String, DateTimeFormatter] =
    Seq("en", "cy").map { code =>
      code -> DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag(code))
    }.toMap

  private val localisedDateTimeFormatters      = localisedFormatters(fullPattern)
  private val localisedShortDateTimeFormatters = localisedFormatters(shortPattern)

  def dateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val languageTag = if (messages.lang.code == "cy") "cy" else "en"
    date.format(dateTimeFormat()(Lang(languageTag)))
  }

  def shortDateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val languageTag = if (messages.lang.code == "cy") "cy" else "en"
    date.format(shortDateTimeFormat()(Lang(languageTag)))
  }

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedDateTimeFormatters.getOrElse(lang.code, DateTimeFormatter.ofPattern(fullPattern))

  def shortDateTimeFormat()(implicit lang: Lang): DateTimeFormatter =
    localisedShortDateTimeFormatters.getOrElse(lang.code, DateTimeFormatter.ofPattern(shortPattern))

  val dateTimeHintFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d M yyyy")

  def formattedSystemTime(clock: Clock)(lang: Lang): String = {
    val pattern   = "hh:mm a"
    val locale    = Locale.forLanguageTag(lang.code)
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    val time      = LocalTime.ofInstant(clock.instant(), ZoneId.systemDefault())
    time.format(formatter)
  }

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

  private def calculateActiveDate(accessStart: LocalDate, dataStart: LocalDate)(implicit messages: Messages): String = {
    val daysDiff = ChronoUnit.DAYS.between(accessStart, dataStart).abs
    val fmt      = dateTimeFormat()(messages.lang)
    if (daysDiff < 3 || dataStart.isAfter(accessStart.plusDays(2))) {
      dataStart.plusDays(2).format(fmt)
    } else {
      accessStart.format(fmt)
    }
  }

  private def daysBetween: (LocalDate, LocalDate) => Long = (a, b) => math.abs(DAYS.between(a, b))

  def computeCalculatedDateValue(
    accessStart: LocalDate,
    accessEnd: Option[LocalDate],
    dataStart: Option[LocalDate],
    dataEnd: Option[LocalDate]
  )(implicit messages: Messages): Option[String] = {
    val isAccessEndEmpty = accessEnd.isEmpty
    val isDataStartEmpty = dataStart.isEmpty
    val isDataEndEmpty   = dataEnd.isEmpty

    val fmt           = dateTimeFormat()(messages.lang)
    val activeDateOpt = dataStart.map(ds => calculateActiveDate(accessStart, ds))
    val longAccess    = accessEnd.exists(end => daysBetween(accessStart, end) > 3)
    val longData      = dataStart.zip(dataEnd).exists { case (start, end) => daysBetween(start, end) > 3 }

    (isAccessEndEmpty, isDataStartEmpty, isDataEndEmpty) match {
      case (false, false, false) =>
        activeDateOpt.filter(_ => longAccess && longData)
      case (true, false, true)   =>
        activeDateOpt
      case (false, true, true)   =>
        Option.when(longAccess)(accessStart.format(fmt))
      case (true, false, false)  =>
        activeDateOpt.filter(_ => longData)
      case _                     =>
        Some(accessStart.format(fmt))
    }
  }

  def computeCalculatedDateValue(details: ThirdPartyDetails, status: UserActiveStatus)(implicit
    messages: Messages
  ): Option[String] =
    if (status == UserActiveStatus.Active) {
      None
    } else {
      computeCalculatedDateValue(
        details.accessStartDate,
        details.accessEndDate,
        details.dataStartDate,
        details.dataEndDate
      )
    }

  def instantToDateString(instant: Instant, clock: Clock)(implicit messages: Messages): String = {
    val zoned = instant.atZone(clock.getZone)
    val fmt   = dateTimeFormat()(messages.lang)
    zoned.toLocalDate.format(fmt)
  }

  def instantToTimeString(instant: Instant, clock: Clock)(implicit messages: Messages): String = {
    val locale    = Locale.forLanguageTag(messages.lang.code)
    val pattern   = "hh:mm a"
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    val time      = instant.atZone(clock.getZone).toLocalTime
    time.format(formatter)
  }

  def instantToDateAndTime(instant: Instant, clock: Clock)(implicit messages: Messages): (String, String) =
    (instantToDateString(instant, clock), instantToTimeString(instant, clock))

  def localDateToInstant(date: LocalDate): Instant =
    date.atStartOfDay(ZoneOffset.UTC).toInstant
}
