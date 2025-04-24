package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AccountsYouHaveAuthorityOverImportFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "accountsYouHaveAuthorityOverImport.error.required"
  val lengthKey = "accountsYouHaveAuthorityOverImport.error.length"
  val maxLength = 100

  val form = new AccountsYouHaveAuthorityOverImportFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
