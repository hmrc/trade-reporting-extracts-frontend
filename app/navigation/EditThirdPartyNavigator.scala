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

package navigation

import models.UserAnswers
import pages.Page
import pages.editThirdParty.{EditDataEndDatePage, EditDataStartDatePage, EditDeclarationDatePage, EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage, EditThirdPartyDataTypesPage, EditThirdPartyReferencePage}
import play.api.mvc.Call
import models.thirdparty.DeclarationDate

class EditThirdPartyNavigator extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case EditThirdPartyAccessEndDatePage(thirdPartyEori)   =>
      _ => controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad(thirdPartyEori)
    case EditThirdPartyDataTypesPage(thirdPartyEori)       =>
      _ => controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad(thirdPartyEori)
    case EditThirdPartyAccessStartDatePage(thirdPartyEori) =>
      _ => controllers.editThirdParty.routes.EditThirdPartyAccessEndDateController.onPageLoad(thirdPartyEori)
    case EditThirdPartyReferencePage(thirdPartyEori)       =>
      _ => controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad(thirdPartyEori)
    case EditDeclarationDatePage(thirdPartyEori)           => declarationDateRoutes(thirdPartyEori)
    case EditDataStartDatePage(thirdPartyEori)             =>
      _ => controllers.editThirdParty.routes.EditDataEndDateController.onPageLoad(thirdPartyEori)
    case EditDataEndDatePage(thirdPartyEori)               =>
      _ => controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad(thirdPartyEori)
  }

  private def declarationDateRoutes(thirdPartyEori: String)(answers: UserAnswers): Call =
    answers
      .get(EditDeclarationDatePage(thirdPartyEori))
      .map {
        case DeclarationDate.AllAvailableData =>
          controllers.thirdparty.routes.ThirdPartyDetailsController.onPageLoad(thirdPartyEori)
        case DeclarationDate.CustomDateRange  =>
          controllers.editThirdParty.routes.EditDataStartDateController.onPageLoad(thirdPartyEori)
      }
      .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())
}
