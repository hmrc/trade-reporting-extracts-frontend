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

package models.report

import base.SpecBase
import models.SectionNavigation
import org.scalatestplus.mockito.MockitoSugar
import pages.report.ReportNamePage

class ReportRequestSectionSpec extends SpecBase with MockitoSugar {

  "ReportRequestSection" - {
    val section    = new ReportRequestSection
    val sectionNav = SectionNavigation("reportRequestSection")

    "navigateTo should return saved url if present" in {
      val answers = emptyUserAnswers.set(sectionNav, "/foo").success.value
      section.navigateTo(answers) mustBe "/foo"
    }

    "navigateTo should return initial page url if not present" in {
      section.navigateTo(emptyUserAnswers) mustBe section.initialPage.url
    }

    "saveNavigation should set urlFragment" in {
      val updatedAnswers = section.saveNavigation(emptyUserAnswers, "foo")
      updatedAnswers.get(sectionNav) mustBe Some("foo")
    }

    "removeAllReportRequestAnswersAndNavigation should remove paths and report data" in {

      val userAnswers = emptyUserAnswers
        .set(sectionNav, "/foo")
        .success
        .value
        .set(ReportNamePage, "foo")
        .success
        .value

      val cleanedAnswers = ReportRequestSection.removeAllReportRequestAnswersAndNavigation(userAnswers)
      cleanedAnswers.get(sectionNav) mustBe None
      cleanedAnswers.get(ReportNamePage) mustBe None
    }
  }
}
