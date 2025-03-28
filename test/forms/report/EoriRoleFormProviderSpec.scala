package forms.report

import forms.behaviours.CheckboxFieldBehaviours
import forms.report.EoriRoleFormProvider
import models.EoriRole
import play.api.data.FormError

class EoriRoleFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new EoriRoleFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "eoriRole.error.required"

    behave like checkboxField[EoriRole](
      form,
      fieldName,
      validValues  = EoriRole.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
