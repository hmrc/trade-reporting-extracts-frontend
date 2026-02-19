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

import models.thirdparty.{DataTypes, DeclarationDate}
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.thirdparty._

import java.time.LocalDate

class ThirdPartyFieldsValidatorSpec extends AnyFreeSpec with Matchers {

  private val emptyUserAnswers = UserAnswers("id")

  "ThirdPartyFieldsValidator" - {

    "should return true when all mandatory fields are provided with consent Yes" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.AllAvailableData)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe true
    }

    "should return false when consent is No regardless of reference being provided" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, false)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyReferencePage, "Test Reference")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.AllAvailableData)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return true when custom date range is provided with start date (end date optional)" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.CustomDateRange)
        .success
        .value
        .set(DataStartDatePage, LocalDate.now().minusDays(30))
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe true
    }

    "should return false when ThirdPartyDataOwnerConsent is missing" in {
      val userAnswers = emptyUserAnswers
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return false when EoriNumber is missing" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return false when consent is No (regardless of ThirdPartyReference)" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, false)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.AllAvailableData)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return false when ThirdPartyAccessStartDate is missing" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.AllAvailableData)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return false when DataTypes is missing or empty" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.AllAvailableData)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return false when DeclarationDate is missing" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return false when custom date range is selected but DataStartDate is missing" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.CustomDateRange)
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe false
    }

    "should return true when custom date range is provided with both start and end dates" in {
      val userAnswers = emptyUserAnswers
        .set(ThirdPartyDataOwnerConsentPage, true)
        .success
        .value
        .set(EoriNumberPage, "GB123456789000")
        .success
        .value
        .set(ThirdPartyAccessStartDatePage, LocalDate.now())
        .success
        .value
        .set(DataTypesPage, Set(DataTypes.Import))
        .success
        .value
        .set(DeclarationDatePage, DeclarationDate.CustomDateRange)
        .success
        .value
        .set(DataStartDatePage, LocalDate.now().minusDays(30))
        .success
        .value
        .set(DataEndDatePage, Some(LocalDate.now()))
        .success
        .value

      val result = ThirdPartyFieldsValidator.validateMandatoryFields(userAnswers)

      result mustBe true
    }
  }
}