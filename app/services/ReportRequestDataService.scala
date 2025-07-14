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
import models.{EoriRole, UserAnswers}
import models.report.Decision.{Export, Import}
import models.report.EmailSelection.Email3
import models.report.{ChooseEori, EmailSelection, ReportDateRange, ReportRequestUserAnswersModel}
import pages.report.{AccountsYouHaveAuthorityOverImportPage, ChooseEoriPage, CustomRequestEndDatePage, CustomRequestStartDatePage, DecisionPage, EmailSelectionPage, EoriRolePage, MaybeAdditionalEmailPage, NewEmailNotificationPage, ReportDateRangePage, ReportNamePage, ReportTypeImportPage}
import config.FrontendAppConfig
import utils.DateTimeFormats

import java.time.{Clock, LocalDate}

class ReportRequestDataService @Inject (clock: Clock = Clock.systemUTC(), appConfig: FrontendAppConfig) {

  def buildReportRequest(userAnswers: UserAnswers, eori: String): ReportRequestUserAnswersModel = {

    val reportDates = getReportDates(userAnswers)

    ReportRequestUserAnswersModel(
      eori = eori,
      dataType = userAnswers.get(DecisionPage).get.toString,
      whichEori = Some(getEori(userAnswers, eori)),
      eoriRole = getEoriRole(userAnswers),
      reportType = userAnswers.get(ReportTypeImportPage).get.map(_.toString),
      reportStartDate = reportDates._1,
      reportEndDate = reportDates._2,
      reportName = userAnswers.get(ReportNamePage).get,
      additionalEmail = getAdditionalEmails(userAnswers)
    )
  }

  private def getEoriRole(userAnswers: UserAnswers): Set[String] =
    if (appConfig.thirdPartyEnabled && userAnswers.get(AccountsYouHaveAuthorityOverImportPage).isDefined) {
      userAnswers.get(DecisionPage).get match {
        case decision if decision == Export => Set(EoriRole.Exporter.toString)
        case decision if decision == Import => Set(EoriRole.Importer.toString)
      }
    } else {
      userAnswers.get(EoriRolePage).get.map(i => i.toString)
    }

  private def getAdditionalEmails(userAnswers: UserAnswers): Option[Set[String]] =
    userAnswers.get(MaybeAdditionalEmailPage) match {
      case Some(true) =>
        userAnswers.get(EmailSelectionPage) match {
          case Some(value) if value.contains(Email3) =>
            Some(
              userAnswers.get(EmailSelectionPage).get.filterNot(_ == Email3).map(_.toString) ++
                Set(userAnswers.get(NewEmailNotificationPage).get)
            )
          case _                                     => Some(userAnswers.get(EmailSelectionPage).get.filterNot(_ == Email3).map(_.toString))
        }
      case _          =>
        None
    }

  private def getReportDates(userAnswers: UserAnswers): (String, String) = {
    val currentDate: LocalDate = LocalDate.now(clock)
    userAnswers.get(ReportDateRangePage) match {
      case Some(ReportDateRange.CustomDateRange)       =>
        (
          userAnswers.get(CustomRequestStartDatePage).get.toString,
          userAnswers.get(CustomRequestEndDatePage).get.toString
        )
      case Some(ReportDateRange.LastFullCalendarMonth) =>
        val startEndDate = DateTimeFormats.lastFullCalendarMonth(currentDate)
        (startEndDate._1.toString, startEndDate._2.toString)
      case _                                           => ("", "")
    }
  }

  private def getEori(userAnswers: UserAnswers, eori: String): String =
    if (appConfig.thirdPartyEnabled) {
      userAnswers.get(ChooseEoriPage) match {
        case Some(ChooseEori.Myeori) => eori
        case _                       => userAnswers.get(AccountsYouHaveAuthorityOverImportPage).get
      }
    } else eori
}
