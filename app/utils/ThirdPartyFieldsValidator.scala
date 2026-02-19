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

package utils

import models.UserAnswers
import models.thirdparty.DeclarationDate
import pages.thirdparty._

object ThirdPartyFieldsValidator {

  def validateMandatoryFields(userAnswers: UserAnswers): Boolean = {
    val validations = Seq(
      validateThirdPartyDataOwnerConsent(userAnswers),
      validateEoriNumber(userAnswers),
      validateThirdPartyReference(userAnswers),
      validateThirdPartyAccessPeriod(userAnswers),
      validateDataTypes(userAnswers),
      validateDeclarationDate(userAnswers),
      validateDataTheyCanView(userAnswers)
    )

    validations.forall(identity)
  }

  private def validateThirdPartyDataOwnerConsent(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ThirdPartyDataOwnerConsentPage).isDefined

  private def validateEoriNumber(userAnswers: UserAnswers): Boolean =
    userAnswers.get(EoriNumberPage).isDefined

  private def validateThirdPartyReference(userAnswers: UserAnswers): Boolean = {
    val hasDataOwnerConsent = userAnswers.get(ThirdPartyDataOwnerConsentPage)
    
    hasDataOwnerConsent match {
      case Some(false) =>
        true
      case Some(true) =>
        true
      case None =>
        false
    }
  }

  private def validateThirdPartyAccessPeriod(userAnswers: UserAnswers): Boolean = {
    val hasStartDate = userAnswers.get(ThirdPartyAccessStartDatePage).isDefined
    hasStartDate
  }

  private def validateDataTypes(userAnswers: UserAnswers): Boolean =
    userAnswers.get(DataTypesPage).exists(_.nonEmpty)

  private def validateDeclarationDate(userAnswers: UserAnswers): Boolean =
    userAnswers.get(DeclarationDatePage).isDefined

  private def validateDataTheyCanView(userAnswers: UserAnswers): Boolean = {
    val declarationDate = userAnswers.get(DeclarationDatePage)
    
    declarationDate match {
      case Some(DeclarationDate.CustomDateRange) =>
        val hasDataStartDate = userAnswers.get(DataStartDatePage).isDefined
        hasDataStartDate
      case Some(DeclarationDate.AllAvailableData) =>
        true
      case None =>
        true
    }
  }
}