package forms

import forms.behaviours.OptionFieldBehaviours
import models.ConfirmEori
import play.api.data.FormError

class ConfirmEoriFormProviderSpec extends OptionFieldBehaviours {

  val form = new ConfirmEoriFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "confirmEori.error.required"

    behave like optionsField[ConfirmEori](
      form,
      fieldName,
      validValues  = ConfirmEori.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
