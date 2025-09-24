package forms

import forms.behaviours.OptionFieldBehaviours
import forms.report.SelectThirdPartyEoriFormProvider
import models.SelectThirdPartyEori
import play.api.data.FormError

class SelectThirdPartyEoriFormProviderSpec extends OptionFieldBehaviours {

  val form = new SelectThirdPartyEoriFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "selectThirdPartyEori.error.required"

    behave like optionsField[SelectThirdPartyEori](
      form,
      fieldName,
      validValues = SelectThirdPartyEori.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
