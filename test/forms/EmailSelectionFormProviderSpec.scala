package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.EmailSelection
import play.api.data.FormError

class EmailSelectionFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new EmailSelectionFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "emailSelection.error.required"

    behave like checkboxField[EmailSelection](
      form,
      fieldName,
      validValues  = EmailSelection.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
