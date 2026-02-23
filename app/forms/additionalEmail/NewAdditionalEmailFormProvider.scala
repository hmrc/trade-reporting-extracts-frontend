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

import forms.mappings.Mappings
import models.StringFieldRegex
import play.api.data.Form

import javax.inject.Inject

class NewAdditionalEmailFormProvider @Inject() extends Mappings {

  def apply(existingEmails: Seq[String] = Seq.empty): Form[String] =
    Form(
      "value" -> text("newAdditionalEmail.error.required")
        .verifying(
          maxLength(100, "newAdditionalEmail.error.length")
        )
        .verifying(
          regexp(StringFieldRegex.emailRegex, "newAdditionalEmail.error.invalidFormat")
        )
        .verifying(
          "newAdditionalEmail.error.alreadyAdded",
          email => !existingEmails.contains(email.toLowerCase.trim)
        )
    )
}
