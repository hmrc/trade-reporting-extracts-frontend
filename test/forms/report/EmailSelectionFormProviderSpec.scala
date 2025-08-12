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

package forms.report

import forms.behaviours.CheckboxFieldBehaviours
import models.report.EmailSelection
import play.api.data.FormError

class EmailSelectionFormProviderSpec extends CheckboxFieldBehaviours {

  val dynamicEmails: Seq[String] = Seq("test1@example.com", "test2@example.com")
  val form                       = new EmailSelectionFormProvider()(dynamicEmails)

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "emailSelection.error.required"

    behave like checkboxField[String](
      form,
      fieldName,
      validValues = dynamicEmails :+ EmailSelection.AddNewEmailValue,
      invalidError = FormError(fieldName, requiredKey)
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
