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
import pages.thirdparty.{DataEndDatePage, DataStartDatePage}
import utils.json.OptionalLocalDateReads.*
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object DataTheyCanViewSummary {

  def checkYourAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(DataStartDatePage).map { answer =>

      implicit val lang: Lang = messages.lang

      val dataEndDate = answers.get(DataEndDatePage)

      val value = dataEndDate match {
        case Some(Some(endDate)) =>
          ValueViewModel(
            messages(
              "dataTheyCanView.fixed.answerLabel",
              answer.format(dateTimeFormat()),
              dataEndDate.get.get.format(dateTimeFormat())
            )
          )
        case _                   =>
          ValueViewModel(
            messages("dataTheyCanView.ongoing.answerLabel", answer.format(dateTimeFormat()))
          )
      }

      SummaryListRowViewModel(
        key = "dataTheyCanView.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.thirdparty.routes.DataStartDateController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("dataTheyCanView.change.hidden"))
        )
      )
    }
  }

  def detailsRow(thirdPartyDetails: ThirdPartyDetails)(implicit messages: Messages): Option[SummaryListRow] = {

    implicit val lang: Lang = messages.lang

    val value = (thirdPartyDetails.dataStartDate, thirdPartyDetails.dataEndDate) match {
      case (Some(startDate), Some(endDate)) =>
        ValueViewModel(
          messages(
            "dataTheyCanView.fixed.answerLabel",
            startDate.format(dateTimeFormat()),
            endDate.format(dateTimeFormat())
          )
        )
      case (Some(startDate), None) =>
        ValueViewModel(
          messages("dataTheyCanView.ongoing.answerLabel", startDate.format(dateTimeFormat()))
        )
      case _ =>
        ValueViewModel(messages("thirdPartyDetails.dataRange.allData"))
    }

    Some(SummaryListRowViewModel(
      key = "dataTheyCanView.checkYourAnswersLabel",
      value = value,
    ))

  }
}
