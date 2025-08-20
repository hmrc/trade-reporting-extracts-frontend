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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.thirdparty.{DataTypesPage, DeclarationDatePage, ThirdPartyDataOwnerConsentPage}
import models.thirdparty.DeclarationDate
import play.api.mvc.Call

class ThirdPartyNavigator @Inject() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(NormalMode)
    case DataTypesPage                  =>
      navigateTo(controllers.routes.DashboardController.onPageLoad())
    case declarationDatePage            => declarationDateRoutes(NormalMode)
  }

  override val checkRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(NormalMode)
    case DataTypesPage                  =>
      navigateTo(controllers.routes.DashboardController.onPageLoad())
    case declarationDatePage            => declarationDateRoutes(NormalMode)
  }

  private def navigateTo(call: => Call): UserAnswers => Call = _ => call

  // TODO CHECKMODE AND ONWARDS NAVIGATION
  private def dataOwnerConsentRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ThirdPartyDataOwnerConsentPage) match {
      case Some(true)  =>
        mode match {
          case NormalMode => controllers.routes.DashboardController.onPageLoad()
          case CheckMode  => controllers.routes.DashboardController.onPageLoad()
        }
      case Some(false) =>
        mode match {
          case NormalMode => controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
          case CheckMode  => controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        }
      case None        => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def declarationDateRoutes(mode: Mode)(answers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        answers
          .get(DeclarationDatePage)
          .map {
            // TODO with TRE-594
            case DeclarationDate.AllAvailableData => controllers.routes.DashboardController.onPageLoad()
            // TODO with TRE-591
            case DeclarationDate.CustomDateRange  => controllers.routes.DashboardController.onPageLoad()
          }
          .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

      case CheckMode =>
        controllers.routes.DashboardController.onPageLoad()
    }
}
