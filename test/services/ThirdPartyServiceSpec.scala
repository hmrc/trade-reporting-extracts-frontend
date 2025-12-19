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

package services

import models.UserAnswers
import models.thirdparty.{DataTypes, DeclarationDate}
import org.scalatest.TryValues.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.thirdparty.*

import java.time.{Clock, Instant, LocalDate, ZoneId}

class ThirdPartyServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  val fixedInstant: Instant = Instant.parse("2024-01-01T00:00:00Z")
  val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
  val service               = new ThirdPartyService(fixedClock)

  "ThirdPartyService" should {

    "buildThirdPartyAddRequest" should {

      "populate all fields correctly when all data is present" in {
        val userAnswers = UserAnswers("id")
          .set(EoriNumberPage, "GB2")
          .success
          .value
          .set(ThirdPartyAccessStartDatePage, LocalDate.of(2024, 1, 1))
          .success
          .value
          .set(DataTypesPage, Set(DataTypes.Import))
          .success
          .value
          .set(ThirdPartyReferencePage, "ref")
          .success
          .value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")

        result.userEORI mustBe "GB1"
        result.thirdPartyEORI mustBe "GB2"
        result.accessType mustBe Set("IMPORT")
        result.accessStart mustBe LocalDate.of(2024, 1, 1).atStartOfDay(fixedClock.getZone).toInstant
        result.accessEnd mustBe None
        result.referenceName mustBe Some("ref")
      }

      "set reportDateStart and reportDateEnd to None for AllAvailableData" in {
        val userAnswers = UserAnswers("id")
          .set(DeclarationDatePage, DeclarationDate.AllAvailableData)
          .success
          .value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        result.reportDateStart mustBe None
        result.reportDateEnd mustBe None
      }

      "set reportDateStart and reportDateEnd for CustomDateRange" in {
        val customDate    = LocalDate.of(2023, 12, 31)
        val customEndDate = LocalDate.of(2025, 12, 31)
        val userAnswers   = UserAnswers("id")
          .set(DeclarationDatePage, DeclarationDate.CustomDateRange)
          .success
          .value
          .set(DataStartDatePage, customDate)
          .success
          .value
          .set(DataEndDatePage, Some(customEndDate))
          .success
          .value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")

        val expectedStartInstant = customDate.atStartOfDay(fixedClock.getZone).toInstant
        val expectedEndInstant   = customEndDate.atStartOfDay(fixedClock.getZone).toInstant

        result.reportDateStart mustBe Some(expectedStartInstant)
        result.reportDateEnd mustBe Some(expectedEndInstant)
      }

      "use current instant for accessStart if not present" in {
        val userAnswers = UserAnswers("id")
        val result      = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        result.accessStart mustBe fixedInstant
      }

      "set accessEnd if present" in {
        val endDate     = LocalDate.of(2024, 2, 1)
        val userAnswers = UserAnswers("id")
          .set(ThirdPartyAccessEndDatePage, Some(endDate))
          .success
          .value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        result.accessEnd mustBe Some(endDate.atStartOfDay(fixedClock.getZone).toInstant)
      }
    }
  }
}
