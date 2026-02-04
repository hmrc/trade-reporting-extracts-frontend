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

package forms.additionalEmail

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class NewAdditionalEmailFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "newAdditionalEmail.error.required"
  val lengthKey = "newAdditionalEmail.error.length"
  val invalidEmailKey = "newAdditionalEmail.error.invalidFormat"
  val maxLength = 100

  val form = new NewAdditionalEmailFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf("test@example.com", "user.name@domain.co.uk", "valid.email@test.org")
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

    "must not bind invalid email addresses" in {
      val invalidEmails = Seq("invalid", "@domain.com", "user@", "user..name@domain.com")
      
      invalidEmails.foreach { email =>
        val result = form.bind(Map(fieldName -> email)).apply(fieldName)
        result.errors.exists(_.message == invalidEmailKey) mustBe true
      }
    }
  }
}
