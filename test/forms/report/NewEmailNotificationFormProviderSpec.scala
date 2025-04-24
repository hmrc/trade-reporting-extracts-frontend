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
import models.StringFieldRegex
import play.api.data.FormError
import utils.Constants.maxNameLength

class NewEmailNotificationFormProviderSpec extends StringFieldBehaviours {

  val requiredKey    = "newEmailNotification.error.required"
  val lengthKey      = "newEmailNotification.error.length"
  val invalidEmail   = "newEmailNotification.error.invalidFormat"
  val maxLength: Int = 100

  val form = new NewEmailNotificationFormProvider()()

  ".value" - {

    val fieldName = "value"

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

    "with invalid email" - {
      behave like fieldThatErrorsOnInvalidData(
        form,
        fieldName,
        stringsWithMaxLength(maxLength),
        FormError(fieldName, invalidEmail)
      )
    }
  }

}
