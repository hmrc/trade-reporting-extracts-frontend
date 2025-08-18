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

package forms.thirdparty

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import utils.Constants.{eoriMaxLength, eoriMinLength}

class EoriNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey      = "eoriNumber.error.required"
  val lengthKey        = "eoriNumber.error.length"
  val minLengthKey     = "eoriNumber.error.minLength"
  val invalidFormatKey = "eoriNumber.error.invalidformat"
  val invalidKey       = "eoriNumber.error.invalidCharacters"
  val maxLength: Int   = eoriMaxLength
  val minLength: Int   = eoriMinLength

  val form = new EoriNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validEoriGen
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = minLength,
      lengthError = FormError(fieldName, minLengthKey, Seq(minLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "with invalid format" - {
      behave like fieldThatErrorsOnInvalidData(
        form,
        fieldName,
        stringsOfExactLength(maxLength),
        FormError(fieldName, invalidFormatKey)
      )
    }

    "with invalid characters" - {
      behave like fieldThatErrorsOnInvalidData(
        form,
        fieldName,
        invalidEoriStringsOfExactLength(maxLength),
        FormError(fieldName, invalidKey)
      )
    }
  }
}
