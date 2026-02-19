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
import models.report.{ChooseEori, EmailSelection, ReportDateRange, ReportRequestUserAnswersModel}
import pages.report.{ChooseEoriPage, CustomRequestEndDatePage, CustomRequestStartDatePage, DecisionPage, EmailSelectionPage, EoriRolePage, MaybeAdditionalEmailPage, NewEmailNotificationPage, ReportDateRangePage, ReportNamePage, ReportTypeImportPage, SelectThirdPartyEoriPage}
import utils.DateTimeFormats

import java.time.{Clock, LocalDate}

class ReportRequestDataService @Inject (clock: Clock = Clock.systemUTC()) {

  def buildReportRequest(userAnswers: UserAnswers, eori: String): Option[ReportRequestUserAnswersModel] =
    for {
      whichEori   <- getEori(userAnswers, eori)
      decision    <- userAnswers.get(DecisionPage)
      reportType  <- userAnswers.get(ReportTypeImportPage)
      reportName  <- userAnswers.get(ReportNamePage)
      reportDates <- getReportDates(userAnswers)
      eoriRole    <- getEoriRole(userAnswers)
    } yield ReportRequestUserAnswersModel(
      eori = eori,
      dataType = decision.toString,
      whichEori = whichEori,
      eoriRole = eoriRole,
      reportType = reportType.map(_.toString),
      reportStartDate = reportDates._1,
      reportEndDate = reportDates._2,
      reportName = reportName,
      additionalEmail = getAdditionalEmails(userAnswers)
    )

  private def getEoriRole(userAnswers: UserAnswers): Option[Set[String]] =
    userAnswers.get(SelectThirdPartyEoriPage) match {
      case Some(_) =>
        userAnswers.get(DecisionPage) match {
          case Some(decision) if decision == Export => Some(Set(EoriRole.Exporter.toString))
          case Some(decision) if decision == Import => Some(Set(EoriRole.Importer.toString))
          case _                                    => None
        }
      case None    =>
        userAnswers.get(EoriRolePage) match {
          case Some(eoriRoles) => Some(eoriRoles.map(_.toString))
          case None            => None
        }
    }

  private def getAdditionalEmails(userAnswers: UserAnswers): Option[Set[String]] =
    userAnswers.get(MaybeAdditionalEmailPage) match {
      case Some(true) =>
        userAnswers.get(EmailSelectionPage).map { selected =>
          val baseEmails      = selected.filterNot(_ == EmailSelection.AddNewEmailValue)
          val additionalEmail = userAnswers.get(NewEmailNotificationPage)
          additionalEmail match {
            case Some(email) if selected.contains(EmailSelection.AddNewEmailValue) => baseEmails + email
            case _                                                                 => baseEmails
          }
        }
      case _          => None
    }

  private def getReportDates(userAnswers: UserAnswers): Option[(String, String)] = {
    val currentDate: LocalDate = LocalDate.now(clock)
    
    def retrieveReportDates = {
      (userAnswers.get(CustomRequestStartDatePage), userAnswers.get(CustomRequestEndDatePage)) match {
        case (Some(startDate), Some(endDate)) => Some(startDate.toString, endDate.toString)
        case _ => None
      }
    }

    (userAnswers.get(SelectThirdPartyEoriPage).isDefined, userAnswers.get(ReportDateRangePage)) match {
      case (false, Some(ReportDateRange.LastFullCalendarMonth)) =>
        val startEndDate = DateTimeFormats.lastFullCalendarMonth(currentDate)
        Some(startEndDate._1.toString, startEndDate._2.toString)
      case (false, Some(ReportDateRange.CustomDateRange))       =>
        retrieveReportDates
      case (true, _) =>
        retrieveReportDates
      case (_, _)                                           =>
        None
    }
  }

  private def getEori(userAnswers: UserAnswers, eori: String): Option[String] =
    userAnswers.get(ChooseEoriPage) match {
      case Some(ChooseEori.Myeori) => Some(eori)
      case Some(ChooseEori.Myauthority) => userAnswers.get(SelectThirdPartyEoriPage)
      case _ => None
    }
}
