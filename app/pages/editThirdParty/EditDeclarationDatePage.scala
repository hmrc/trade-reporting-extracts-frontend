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

package pages.editThirdParty

import models.UserAnswers
import models.thirdparty.DeclarationDate
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class EditDeclarationDatePage(thirdPartyEori: String) extends QuestionPage[DeclarationDate] {

  override def path: JsPath = JsPath \ "editThirdParty" \ thirdPartyEori \ toString

  override def toString: String = "editDeclarationDate"

  override def cleanup(value: Option[DeclarationDate], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(DeclarationDate.CustomDateRange)  =>
        super.cleanup(value, userAnswers)
      case Some(DeclarationDate.AllAvailableData) =>
        userAnswers
          .remove(EditDataStartDatePage(thirdPartyEori: String))
          .flatMap(_.remove(EditDataEndDatePage(thirdPartyEori: String)))
      case None                                   =>
        super.cleanup(value, userAnswers)
    }
}
