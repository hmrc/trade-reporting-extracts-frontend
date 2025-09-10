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

import models.thirdparty.DataTypes
import models.{CheckMode, UserAnswers}
import pages.thirdparty.DataTypesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object DataTypesSummary {

  def checkYourAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DataTypesPage).map { answers =>

      val value = ValueViewModel(
        HtmlContent(
          answers
            .map { answer =>
              HtmlFormat.escape(messages(s"dataTypes.$answer")).toString
            }
            .mkString(",<br>")
        )
      )

      SummaryListRowViewModel(
        key = "dataTypes.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.thirdparty.routes.DataTypesController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("dataTypes.change.hidden"))
        )
      )
    }

  def detailsRow(dataTypes: Set[String])(implicit messages: Messages): Option[SummaryListRow] = {

    val dataTypeObjects: Set[DataTypes] = dataTypes.collect {
      case "imports" => DataTypes.Import
      case "exports" => DataTypes.Export
    }

    val value = ValueViewModel(
      HtmlContent(
        dataTypeObjects
          .map { answer =>
            HtmlFormat.escape(messages(s"dataTypes.$answer")).toString
          }
          .mkString(",<br>")
      )
    )

    Some(
      SummaryListRowViewModel(
        key = "thirdPartyDetails.dataTypes.label",
        value = value
      )
    )
  }
}
