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
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.JsPath

class SectionNavigationSpec extends SpecBase with Matchers {

  "SectionNavigation" - {
    "should store sectionIdentifier" in {
      val nav = SectionNavigation("reportRequestSection")
      nav.sectionIdentifier shouldBe "reportRequestSection"
    }

    "path should be JsPath \\ navigation \\ sectionIdentifier" in {
      val nav          = SectionNavigation("reportRequestSection")
      val expectedPath = JsPath \ "navigation" \ "reportRequestSection"
      nav.path shouldBe expectedPath
    }
  }
}
