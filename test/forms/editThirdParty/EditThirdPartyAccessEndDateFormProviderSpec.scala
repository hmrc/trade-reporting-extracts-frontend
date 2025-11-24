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

package forms.editThirdParty

import forms.EditThirdPartyAccessEndDateFormProvider
import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class EditThirdPartyAccessEndDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()

  private val fixedToday        = LocalDate.of(2025, 1, 2)
  private val fixedClock: Clock =
    Clock.fixed(Instant.parse("2025-01-02T00:00:00Z"), ZoneOffset.UTC)

  "EditThirdPartyAccessEndDateFormProvider when start date is in the future" - {
    val startDate = LocalDate.of(2025, 1, 5)
    val form      = new EditThirdPartyAccessEndDateFormProvider(fixedClock)(startDate)

    "must allow an empty value (optional field)" in {
      val result = form.bind(Map.empty[String, String])
      result.errors mustBe Nil
      result.value mustBe Some(None)
    }

    "must reject an end date equal to start date (strictly greater required)" in {
      val data   = Map("value.day" -> "5", "value.month" -> "1", "value.year" -> "2025")
      val result = form.bind(data)
      result.errors.map(_.message) must contain only "editThirdPartyAccessEndDate.error.min"
    }

    "must accept an end date of startDate + 1 day" in {
      val data   = Map("value.day" -> "6", "value.month" -> "1", "value.year" -> "2025")
      val result = form.bind(data)
      result.errors mustBe Nil
      result.value mustBe Some(Some(LocalDate.of(2025, 1, 6)))
    }
  }

  "EditThirdPartyAccessEndDateFormProvider when start date is today or before" - {
    val startDate = fixedToday
    val form      = new EditThirdPartyAccessEndDateFormProvider(fixedClock)(startDate)
    "must reject an end date before today" in {
      val data   = Map("value.day" -> "1", "value.month" -> "1", "value.year" -> "2025")
      val result = form.bind(data)
      result.errors.map(_.message) must contain only "editThirdPartyAccessEndDate.error.min"
    }

    "must accept an end date equal to today" in {
      val data   = Map("value.day" -> "2", "value.month" -> "1", "value.year" -> "2025")
      val result = form.bind(data)
      result.errors mustBe Nil
      result.value mustBe Some(Some(fixedToday))
    }

    "must accept an end date after today" in {
      val data   = Map("value.day" -> "3", "value.month" -> "1", "value.year" -> "2025")
      val result = form.bind(data)
      result.errors mustBe Nil
      result.value mustBe Some(Some(LocalDate.of(2025, 1, 3)))
    }
  }
}
