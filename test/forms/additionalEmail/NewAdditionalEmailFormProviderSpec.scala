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

  val requiredKey     = "newAdditionalEmail.error.required"
  val lengthKey       = "newAdditionalEmail.error.length"
  val invalidEmailKey = "newAdditionalEmail.error.invalidFormat"
  val duplicateKey    = "newAdditionalEmail.error.alreadyAdded"
  val maxLength       = 100

  val formProvider = new NewAdditionalEmailFormProvider()
  val form         = formProvider()

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

    "must not bind duplicate emails when existing emails are provided" in {
      val existingEmails   = Seq("existing1@example.com", "existing2@test.org")
      val formWithExisting = formProvider(existingEmails)

      existingEmails.foreach { email =>
        val result = formWithExisting.bind(Map(fieldName -> email))
        result.hasErrors mustBe true
        result.errors.head.message mustEqual duplicateKey
      }
    }

    "must not bind duplicate emails with different case" in {
      val existingEmails   = Seq("existing@example.com", "test@domain.org")
      val formWithExisting = formProvider(existingEmails)

      val testEmails = Seq("EXISTING@EXAMPLE.COM", "Test@Domain.Org")

      testEmails.foreach { email =>
        val result = formWithExisting.bind(Map(fieldName -> email))
        result.hasErrors mustBe true
        result.errors.head.message mustEqual duplicateKey
      }
    }

    "must bind emails successfully when no existing emails are provided" in {
      val formWithEmptyList = formProvider(Seq.empty)

      val testEmails = Seq("test@example.com", "user@test.org", "any@email.co.uk")

      testEmails.foreach { email =>
        val result = formWithEmptyList.bind(Map(fieldName -> email))
        result.hasErrors mustBe false
        result.get mustEqual email
      }
    }

    "must validate email format even with existing emails" in {
      val existingEmails   = Seq("existing@example.com")
      val formWithExisting = formProvider(existingEmails)

      val invalidEmails = Seq("invalid", "@domain.com", "user@", "user..name@domain.com")

      invalidEmails.foreach { email =>
        val result = formWithExisting.bind(Map(fieldName -> email))
        result.hasErrors mustBe true
        result.errors.exists(_.message == invalidEmailKey) mustBe true
      }
    }

    "must enforce length limits even with existing emails" in {
      val existingEmails   = Seq("existing@example.com")
      val formWithExisting = formProvider(existingEmails)

      val tooLongEmail = "a" * 90 + "@example.com" // Creates an email longer than 100 characters

      val result = formWithExisting.bind(Map(fieldName -> tooLongEmail))
      result.hasErrors mustBe true
      result.errors.exists(_.message == lengthKey) mustBe true
    }
  }
}
