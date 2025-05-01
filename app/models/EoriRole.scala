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

sealed trait EoriRole

object EoriRole extends Enumerable.Implicits {

  case object Declarant extends WithName("declarant") with EoriRole
  case object Exporter extends WithName("exporter") with EoriRole
  case object Importer extends WithName("importer") with EoriRole

  val values: Seq[EoriRole] = Seq(
    Declarant,
    Exporter,
    Importer
  )

  def checkboxItems(isImporter: Boolean)(implicit messages: Messages): Seq[CheckboxItem] = {
    val filteredValues =
      if (isImporter) values.filterNot(_ == Exporter)
      else values.filterNot(_ == Importer)

    filteredValues.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"eoriRole.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }
  }

  implicit val enumerable: Enumerable[EoriRole] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
