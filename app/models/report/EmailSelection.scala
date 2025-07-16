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

  val AddNewEmailValue: String = "AddNewEmail"

  case object AddNewEmail extends WithName(AddNewEmailValue) with EmailSelection

  def checkboxItems(dynamicEmails: Seq[String])(implicit messages: Messages): Seq[CheckboxItem] = {
    val dynamicItems = dynamicEmails.zipWithIndex.map { case (email, index) =>
      CheckboxItemViewModel(
        content = Text(email),
        fieldId = "value",
        index = index,
        value = email
      )
    }

    val addAnotherItem = CheckboxItemViewModel(
      content = Text(messages("emailSelection.addNewEmail")),
      fieldId = "value",
      index = dynamicEmails.length,
      value = AddNewEmail.toString
    )

    dynamicItems :+ addAnotherItem
  }

  // Enumerable instance for binding form values
  implicit val enumerable: Enumerable[EmailSelection] =
    Enumerable(
      AddNewEmail.toString -> AddNewEmail
    )
}
