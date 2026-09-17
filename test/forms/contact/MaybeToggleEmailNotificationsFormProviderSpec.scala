/*
 * Copyright 2026 HM Revenue & Customs
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

package forms.contact

import forms.behaviours.BooleanFieldBehaviours
import forms.contact.MaybeToggleEmailNotificationsFormProvider
import play.api.data.FormError

class MaybeToggleEmailNotificationsFormProviderSpec extends BooleanFieldBehaviours {

  val enableRequiredKey  = "maybeToggleEmailNotifications.error.required.enable"
  val disableRequiredKey = "maybeToggleEmailNotifications.error.required.disable"
  val invalidKey         = "error.boolean"

  val enableForm  = new MaybeToggleEmailNotificationsFormProvider()(true)
  val disableForm = new MaybeToggleEmailNotificationsFormProvider()(false)

  val fieldName = "value"

  "disabling email" - {

    behave like booleanField(
      disableForm,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      disableForm,
      fieldName,
      requiredError = FormError(fieldName, disableRequiredKey)
    )
  }

  "enabling email" - {

    behave like booleanField(
      enableForm,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      enableForm,
      fieldName,
      requiredError = FormError(fieldName, enableRequiredKey)
    )
  }
}
