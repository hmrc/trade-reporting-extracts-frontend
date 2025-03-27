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
import play.api.data.{Form, FormError}

class DecisionFormProviderSpec extends StringFieldBehaviours {

  private val formProvider       = new DecisionFormProvider()
  private val form: Form[String] = formProvider()
  private val requiredKey        = "decisionPage.error.required"
  private val fieldName          = "value"

  ".value" - {

    "must bind valid data" in {
      val validData = "Some decision"
      val result    = form.bind(Map(fieldName -> validData)).apply(fieldName)
      result.value.value mustEqual validData
    }

    "must not bind empty data" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "must not bind data with only spaces" in {
      val result = form.bind(Map(fieldName -> "   ")).apply(fieldName)
      result.errors must contain only FormError(fieldName, requiredKey)
    }
  }
}
