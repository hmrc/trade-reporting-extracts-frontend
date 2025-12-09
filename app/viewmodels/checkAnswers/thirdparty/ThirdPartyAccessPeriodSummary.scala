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

package viewmodels.checkAnswers.thirdparty

import models.{CheckMode, ThirdPartyDetails, UserAnswers}
import pages.editThirdParty.{EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage}
import pages.thirdparty.{ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage}
import utils.json.OptionalLocalDateReads.*
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate

object ThirdPartyAccessPeriodSummary {

  def checkYourAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ThirdPartyAccessStartDatePage).map { answer =>

      implicit val lang: Lang = messages.lang

      val thirdPartyEndDate = answers.get(ThirdPartyAccessEndDatePage)

      val value = thirdPartyEndDate match {
        case Some(Some(endDate)) =>
          ValueViewModel(
            messages(
              "thirdPartyAccessPeriod.fixed.answerLabel",
              answer.format(dateTimeFormat()),
              thirdPartyEndDate.get.get.format(dateTimeFormat())
            )
          )
        case _                   =>
          ValueViewModel(
            messages("thirdPartyAccessPeriod.ongoing.answerLabel", answer.format(dateTimeFormat()))
          )
      }

      SummaryListRowViewModel(
        key = "thirdPartyAccessPeriod.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("thirdPartyAccessPeriod.change.hidden"))
        )
      )
    }

  def detailsRow(
    thirdPartyDetails: ThirdPartyDetails,
    isThirdPartyEnabled: Boolean,
    thirdPartyEori: String,
    answers: UserAnswers
  )(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    Some(
      buildRow(
        answers
          .get(EditThirdPartyAccessStartDatePage(thirdPartyEori))
          .getOrElse(thirdPartyDetails.accessStartDate),
        answers
          .get(EditThirdPartyAccessEndDatePage(thirdPartyEori))
          .orElse(thirdPartyDetails.accessEndDate),
        "thirdPartyAccessPeriod.checkYourAnswersLabel",
        isThirdPartyEnabled,
        Some(thirdPartyEori)
      )
    )

  def businessDetailsRow(thirdPartyDetails: ThirdPartyDetails)(implicit messages: Messages): Option[SummaryListRow] =
    Some(
      buildRow(
        thirdPartyDetails.accessStartDate,
        thirdPartyDetails.accessEndDate,
        "thirdPartyAccessPeriod.checkYourAnswersLabel",
        false,
        None
      )
    )

  private def buildRow(
    startDate: LocalDate,
    maybeEndDate: Option[LocalDate],
    keyMessage: String,
    tpEnabledAndNotBusinessDetailsRow: Boolean,
    thirdPartyEori: Option[String]
  )(implicit messages: Messages): SummaryListRow = {

    implicit val lang: Lang = messages.lang

    val value = maybeEndDate match {
      case Some(endDate) if endDate == LocalDate.MAX =>
        ValueViewModel(
          messages(
            "thirdPartyAccessPeriod.ongoing.answerLabel",
            startDate.format(dateTimeFormat())
          )
        )
      case Some(endDate)                             =>
        ValueViewModel(
          messages(
            "thirdPartyAccessPeriod.fixed.answerLabel",
            startDate.format(dateTimeFormat()),
            endDate.format(dateTimeFormat())
          )
        )
      case _                                         =>
        ValueViewModel(
          messages(
            "thirdPartyAccessPeriod.ongoing.answerLabel",
            startDate.format(dateTimeFormat())
          )
        )
    }

    if (tpEnabledAndNotBusinessDetailsRow && thirdPartyEori.isDefined) {
      SummaryListRowViewModel(
        key = keyMessage,
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.editThirdParty.routes.EditThirdPartyAccessStartDateController.onPageLoad(thirdPartyEori.get).url
          )
            .withVisuallyHiddenText(messages("thirdPartyAccessPeriod.change.hidden"))
        )
      )
    } else {
      SummaryListRowViewModel(
        key = keyMessage,
        value = value
      )
    }
  }
}
