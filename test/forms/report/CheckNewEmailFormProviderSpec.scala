package forms.report

import forms.behaviours.BooleanFieldBehaviours
import forms.report.CheckNewEmailFormProvider
import play.api.data.FormError

class CheckNewEmailFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "checkNewEmail.error.required"
  val invalidKey = "error.boolean"

  val form = new CheckNewEmailFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
