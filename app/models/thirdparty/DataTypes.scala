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

package models.thirdparty

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import play.api.libs.json._
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait DataTypes

object DataTypes extends Enumerable.Implicits {

  case object Import extends WithName("import") with DataTypes
  case object Export extends WithName("export") with DataTypes

  val values: Seq[DataTypes] = Seq(
    Import,
    Export
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"dataTypes.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[DataTypes] =
    Enumerable(values.map(v => v.toString -> v): _*)

  implicit val dataTypesReads: Reads[DataTypes] = Reads {
    case JsString(str) =>
      enumerable.withName(str).map(JsSuccess(_)).getOrElse(JsError(s"Unknown DataTypes: $str"))
    case _             => JsError("Expected DataTypes as JsString")
  }

  implicit val dataTypesWrites: Writes[DataTypes] = Writes { dt =>
    JsString(dt.toString)
  }

  implicit val dataTypesFormat: Format[DataTypes] = Format(dataTypesReads, dataTypesWrites)

  implicit val dataTypesSeqReads: Reads[Seq[DataTypes]]   = Reads.seq(dataTypesReads)
  implicit val dataTypesSeqWrites: Writes[Seq[DataTypes]] = Writes.seq(dataTypesWrites)
  implicit val dataTypesSeqFormat: Format[Seq[DataTypes]] = Format(dataTypesSeqReads, dataTypesSeqWrites)
}
