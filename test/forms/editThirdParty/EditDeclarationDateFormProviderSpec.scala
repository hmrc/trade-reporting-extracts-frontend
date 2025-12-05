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

package forms.editThirdParty

import forms.behaviours.OptionFieldBehaviours
import forms.editThirdParty.EditDeclarationDateFormProvider
import models.thirdparty.DeclarationDate
import play.api.data.{Form, FormError}

class EditDeclarationDateFormProviderSpec extends OptionFieldBehaviours {

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "editDeclarationDate.error.required"

    behave like optionsField[DeclarationDate](
      newForm(),
      fieldName,
      validValues = DeclarationDate.values,
      invalidError = FormError(fieldName, "error.invalid", Seq(""))
    )

    behave like mandatoryField(
      newForm(),
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(""))
    )
  }

  private def newForm(): Form[DeclarationDate] = {
    val form = new EditDeclarationDateFormProvider()
    form(Seq(""))
  }
}
