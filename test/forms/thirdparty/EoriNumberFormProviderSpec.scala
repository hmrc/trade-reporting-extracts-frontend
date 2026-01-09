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
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.data.{Form, FormError}
import utils.Constants.{eoriMaxLength, eoriMinLength}

class EoriNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey      = "eoriNumber.error.required"
  val lengthKey        = "eoriNumber.error.length"
  val minLengthKey     = "eoriNumber.error.minLength"
  val invalidFormatKey = "eoriNumber.error.invalidFormat"
  val invalidKey       = "eoriNumber.error.invalidCharacters"
  val maxLength: Int   = eoriMaxLength
  val minLength: Int   = eoriMinLength

  val userEori           = "GB123456789000"
  val form: Form[String] = new EoriNumberFormProvider().apply(userEori)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validGBEoriGen
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

    "must error on strings with invalid special characters" in {
      val testCase = "GB12345678901!"
      val result   = form.bind(Map(fieldName -> testCase))
      result.errors                should not be empty
      result.errors.head.message shouldBe invalidKey
    }

    "must error on strings with invalid format" in {
      val testCase = "12345678901234"
      val result   = form.bind(Map(fieldName -> testCase))
      result.errors                should not be empty
      result.errors.head.message shouldBe invalidFormatKey
    }

    "convert lowercase to uppercase" - {
      "must convert lowercase EORI to uppercase" in {
        val result = form.bind(Map(fieldName -> "gb123456789001"))
        result.value  shouldBe Some("GB123456789001")
        result.errors shouldBe empty
      }

      "must convert mixed case EORI to uppercase" in {
        val result = form.bind(Map(fieldName -> "Gb123456789001"))
        result.value  shouldBe Some("GB123456789001")
        result.errors shouldBe empty
      }
    }
  }
}
