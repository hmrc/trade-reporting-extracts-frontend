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

import controllers.routes
import models.thirdparty.DataTypes
import models.{CheckMode, UserAnswers}
import pages.thirdparty.{DataTypesPage, DeclarationDatePage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object DeclarationDateSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val dataTypes = getDataTypesString(answers.get(DataTypesPage))

    answers.get(DeclarationDatePage).map { answer =>

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"declarationDate.$answer"))
        )
      )

      SummaryListRowViewModel(
        key = messages("declarationDate.checkYourAnswersLabel", dataTypes),
        value = value,
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            controllers.thirdparty.routes.DeclarationDateController.onPageLoad(CheckMode).url
          )
            .withVisuallyHiddenText(messages("declarationDate.change.hidden"))
        )
      )
    }
  }

  def getDataTypesString(dataTypesAnswer: Option[Set[DataTypes]])(implicit messages: Messages): String =
    dataTypesAnswer match {
      case Some(set) if set == Set(DataTypes.Import)                   => messages("declarationDate.import")
      case Some(set) if set == Set(DataTypes.Export)                   => messages("declarationDate.export")
      case Some(set) if set == Set(DataTypes.Import, DataTypes.Export) => messages("declarationDate.importExport")
      case _                                                           => ""
    }
}
