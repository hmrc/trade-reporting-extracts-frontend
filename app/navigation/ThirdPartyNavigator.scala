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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.thirdparty._
import play.api.mvc.Call

class ThirdPartyNavigator @Inject() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(NormalMode)
    case DataTypesPage                  =>
      navigateTo(controllers.routes.DashboardController.onPageLoad())
    case EoriNumberPage                 =>
      navigateTo(controllers.thirdparty.routes.ConfirmEoriController.onPageLoad(NormalMode))
  }

  override val checkRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage => dataOwnerConsentRoutes(CheckMode)
    case ThirdPartyReferencePage        => thirdPartyReferenceRoutes(CheckMode)
  }

  override val checkRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(CheckMode)
    case DataTypesPage                  =>
      navigateTo(controllers.routes.DashboardController.onPageLoad())
    case EoriNumberPage                 =>
      navigateTo(controllers.routes.DashboardController.onPageLoad())
    case ThirdPartyReferencePage        => thirdPartyReferenceRoutes(CheckMode)
  }

  private def navigateTo(call: => Call): UserAnswers => Call = _ => call

  private def thirdPartyReferenceRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ThirdPartyReferencePage) match {
      case Some(_) =>
        mode match {
          // CHANGE TO ACCESS START PAGE
          case NormalMode => controllers.routes.DashboardController.onPageLoad()
          // CHANGE FOR CHECKMODE
          case CheckMode  => controllers.thirdparty.routes.ThirdPartyDataOwnerConsentController.onPageLoad(CheckMode)
        }
      case None    => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  // TODO CHECKMODE AND ONWARDS NAVIGATION
  private def dataOwnerConsentRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ThirdPartyDataOwnerConsentPage) match {
      case Some(true)  =>
        mode match {
          case NormalMode => controllers.thirdparty.routes.EoriNumberController.onPageLoad(NormalMode)
          case CheckMode  => controllers.routes.DashboardController.onPageLoad()
        }
      case Some(false) =>
        mode match {
          case NormalMode => controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
          case CheckMode  => controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        }
      case None        => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }
}
