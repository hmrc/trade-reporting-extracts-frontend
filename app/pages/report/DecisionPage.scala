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

package pages.report

import models.UserAnswers
import models.report.Decision
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object DecisionPage extends QuestionPage[Decision] {

  override def path: JsPath = JsPath \ "report" \ toString

  override def toString: String = "decision"

  override def cleanup(value: Option[Decision], userAnswers: UserAnswers): Try[UserAnswers] =
    value
      .map { _ =>
        userAnswers
          .remove(EoriRolePage)
          .flatMap(_.remove(ReportTypeImportPage))
      }
      .getOrElse(super.cleanup(value, userAnswers))
}
