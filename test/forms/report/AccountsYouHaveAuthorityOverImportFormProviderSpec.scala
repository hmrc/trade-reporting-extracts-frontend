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

import forms.behaviours.StringFieldBehaviours
import forms.report.AccountsYouHaveAuthorityOverImportFormProvider
import generators.Generators
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class AccountsYouHaveAuthorityOverImportFormProviderSpec extends StringFieldBehaviours with Generators {

  private implicit val messages: Messages = stubMessages()

  val requiredKey  = "accountsYouHaveAuthorityOverImport.error.required"
  val defaultValue = messages("accountsYouHaveAuthorityOverImport.defaultValue")

  val form = new AccountsYouHaveAuthorityOverImportFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonDefaultStrings(defaultValue)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatErrorsOnInvalidData(
      form,
      fieldName,
      Gen.const(defaultValue),
      invalidError = FormError(fieldName, requiredKey)
    )
  }
}
