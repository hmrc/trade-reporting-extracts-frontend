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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait ReportTypeImport

object ReportTypeImport extends Enumerable.Implicits {

  case object ImportHeader extends WithName("importHeader") with ReportTypeImport
  case object ImportItem extends WithName("importItem") with ReportTypeImport
  case object ImportTaxLine extends WithName("importTaxLine") with ReportTypeImport
  case object ExportItem extends WithName("exportItem") with ReportTypeImport

  val values: Seq[ReportTypeImport] = Seq(
    ImportHeader,
    ImportItem,
    ImportTaxLine,
    ExportItem
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] = {
    val filteredValues = values.filterNot(_ == ExportItem)
    filteredValues.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"reportTypeImport.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }
  }

  implicit val enumerable: Enumerable[ReportTypeImport] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
