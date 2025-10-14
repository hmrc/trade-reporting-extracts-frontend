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

package models

import base.SpecBase

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsError, JsString, JsSuccess}

class UserActiveStatusSpec extends SpecBase {

  "UserActiveStatus.fromInstants" - {

    val clock      = Clock.fixed(Instant.parse("2025-10-09T00:00:00Z"), ZoneOffset.UTC)
    val today      = LocalDate.now(clock).atStartOfDay()
    val cutoffDate = today.minusDays(3)

    "return Active when access started and report data started" in {
      val accessStart     = today.minusDays(1).toInstant(ZoneOffset.UTC)
      val reportDataStart = Some(cutoffDate.toInstant(ZoneOffset.UTC))

      val status = UserActiveStatus.fromInstants(accessStart, reportDataStart, clock)

      status mustEqual UserActiveStatus.Active
      status.cssClass mustEqual "govuk-tag--green"
      status.displayName mustEqual "Active"
    }

    "return Upcoming when accessStart is after today" in {
      val accessStart     = today.plusDays(2).toInstant(ZoneOffset.UTC)
      val reportDataStart = None

      val status = UserActiveStatus.fromInstants(accessStart, reportDataStart, clock)

      status mustEqual UserActiveStatus.Upcoming
      status.cssClass mustEqual "govuk-tag--blue"
      status.displayName mustEqual "Upcoming"
    }

    "return upcoming when reportDataStart is after cutoffDate" in {
      val accessStart     = today.minusDays(1).toInstant(ZoneOffset.UTC)
      val reportDataStart = Some(cutoffDate.plusDays(1).toInstant(ZoneOffset.UTC))

      val status = UserActiveStatus.fromInstants(accessStart, reportDataStart, clock)

      status mustEqual UserActiveStatus.Upcoming
      status.cssClass mustEqual "govuk-tag--blue"
      status.displayName mustEqual "Upcoming"
    }
  }

  "UserActiveStatus JSON format" - {
    "serialize to JsString" in {
      UserActiveStatus.userActiveStatusFormat.writes(UserActiveStatus.Active) mustEqual JsString("Active")
      UserActiveStatus.userActiveStatusFormat.writes(UserActiveStatus.Upcoming) mustEqual JsString("Upcoming")
    }

    "deserialize from JsString" in {
      UserActiveStatus.userActiveStatusFormat.reads(JsString("Active")) mustEqual JsSuccess(UserActiveStatus.Active)
      UserActiveStatus.userActiveStatusFormat.reads(JsString("Upcoming")) mustEqual JsSuccess(UserActiveStatus.Upcoming)
    }

    "fail to deserialize unknown value" in {
      UserActiveStatus.userActiveStatusFormat.reads(JsString("Unknown")) mustEqual JsError("Unknown UserActiveStatus")
    }
  }
}
