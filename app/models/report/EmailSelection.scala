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

package models.report

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox.*

sealed trait EmailSelection

object EmailSelection extends Enumerable.Implicits {

  case object Email1 extends WithName("email1") with EmailSelection
  case object Email2 extends WithName("email2") with EmailSelection
  case object Email3 extends WithName("email3") with EmailSelection

  val values: Seq[EmailSelection] = Seq(
    Email1,
    Email2,
    Email3
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"emailSelection.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[EmailSelection] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
