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

import models.{SectionNavigation, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

class ReportRequestSection {

  val initialPage: Call                    = controllers.report.routes.ReportGuidanceController.onPageLoad()
  val checkYourAnswersPage: Call           = controllers.report.routes.CheckYourAnswersController.onPageLoad()
  val sectionNavigation: SectionNavigation = SectionNavigation("reportRequestSection")

  def navigateTo(answers: UserAnswers): String                               =
    answers.get(sectionNavigation).getOrElse(controllers.report.routes.ReportGuidanceController.onPageLoad().url)
  def saveNavigation(answers: UserAnswers, urlFragment: String): UserAnswers =
    answers.set(sectionNavigation, urlFragment).get
}

object ReportRequestSection {

  def removeAllReportRequestAnswersAndNavigation(answers: UserAnswers): UserAnswers =
    answers.removePath(SectionNavigation("reportRequestSection").path).get.removePath(JsPath \ "report").get
}
