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

package services

import com.google.inject.Inject
import models.UserAnswers
import models.thirdparty.{DataTypes, DeclarationDate, ThirdPartyRequest}
import pages.thirdparty.{DataStartDatePage, DataTypesPage, DeclarationDatePage, EoriNumberPage, ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage, ThirdPartyReferencePage}
import play.api.libs.json.Reads

import java.time.format.DateTimeFormatter
import java.time.{Clock, Instant, LocalDate}

implicit val localDateReads: Reads[LocalDate]               = Reads.localDateReads(DateTimeFormatter.ISO_LOCAL_DATE)
implicit val optionLocalDateReads: Reads[Option[LocalDate]] = Reads.optionWithNull[LocalDate]

class ThirdPartyService @Inject() (clock: Clock = Clock.systemUTC()) {
  def buildThirdPartyAddRequest(userAnswers: UserAnswers, eori: String): ThirdPartyRequest = {
    val reportAccessDates                = getReportAccessDates(userAnswers)
    val (reportDateStart, reportDateEnd) = reportAccessDates
    val toInstant: LocalDate => Instant  = d => d.atStartOfDay(clock.getZone).toInstant

    ThirdPartyRequest(
      userEORI = eori,
      thirdPartyEORI = userAnswers.get(EoriNumberPage).getOrElse(""),
      accessStart = userAnswers
        .get(ThirdPartyAccessStartDatePage)
        .map(_.atStartOfDay(clock.getZone).toInstant)
        .getOrElse(clock.instant()),
      accessEnd = userAnswers.get(ThirdPartyAccessEndDatePage).flatten.map(toInstant),
      reportDateStart = reportDateStart,
      reportDateEnd = reportDateEnd,
      accessType = userAnswers.get(DataTypesPage).fold(Set.empty[String])(_.map(_.toString.toUpperCase())),
      referenceName = Some(userAnswers.get(ThirdPartyReferencePage).getOrElse(""))
    )
  }

  private def getReportAccessDates(userAnswers: UserAnswers): (Option[Instant], Option[Instant]) =
    userAnswers.get(DeclarationDatePage) match {
      case Some(DeclarationDate.AllAvailableData) =>
        (None, None)
      case Some(DeclarationDate.CustomDateRange)  =>
        (
          userAnswers.get(DataStartDatePage).map(_.atStartOfDay(clock.getZone).toInstant),
          userAnswers.get(DataStartDatePage).map(date => date.atStartOfDay(clock.getZone).toInstant)
        )
      case None                                   => (None, None)
    }
}
