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

import forms.mappings.Mappings
import models.StringFieldRegex
import play.api.data.Form
import play.api.data.validation.Constraints.minLength
import utils.Constants.{eoriMaxLength, eoriMinLength}

import javax.inject.Inject

class EoriNumberFormProvider @Inject() extends Mappings {

  def apply(userEori: String): Form[String] =
    Form(
      "value" -> text("eoriNumber.error.required")
        .transform(_.trim.toUpperCase, identity)
        .verifying(maxLength(eoriMaxLength, "eoriNumber.error.length"))
        .verifying(minLength(eoriMinLength, "eoriNumber.error.minLength"))
        .verifying(
          regexp(
            StringFieldRegex.eoriNumberRegex,
            "eoriNumber.error.invalidCharacters"
          )
        )
        .verifying(
          regexp(
            StringFieldRegex.eoriFormatRegex,
            "eoriNumber.error.invalidFormat"
          )
        )
        .verifying("eoriNumber.error.cannotBeOwnEori", eori => !eori.equalsIgnoreCase(userEori))
    )
}
