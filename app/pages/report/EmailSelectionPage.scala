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
import models.report.EmailSelection
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

case object EmailSelectionPage extends QuestionPage[Set[String]] {

  override def path: JsPath = JsPath \ "report" \ toString

  override def toString: String = "emailSelection"

  override def cleanup(value: Option[Set[String]], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(selections) if !selections.contains(EmailSelection.AddNewEmailValue) =>
        userAnswers.remove(NewEmailNotificationPage)
      case _                                                                         =>
        Success(userAnswers)
    }
}
