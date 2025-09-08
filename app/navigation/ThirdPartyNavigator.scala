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
import controllers.thirdparty.routes
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.thirdparty.*
import models.thirdparty.{ConfirmEori, DeclarationDate}
import play.api.mvc.Call

class ThirdPartyNavigator @Inject() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(NormalMode)
    case DataTypesPage                  =>
      navigateTo(controllers.thirdparty.routes.DeclarationDateController.onPageLoad(NormalMode))
    case EoriNumberPage                 =>
      navigateTo(controllers.thirdparty.routes.ConfirmEoriController.onPageLoad(NormalMode))
    case ThirdPartyReferencePage        => thirdPartyReferenceRoutes(NormalMode)
    case ThirdPartyAccessStartDatePage  => accessStartDateRoutes(NormalMode)
    case DeclarationDatePage            => declarationDateRoutes(NormalMode)
    case DataStartDatePage              => dataStartDateRoutes(NormalMode)
    case DataEndDatePage                => navigateTo(controllers.routes.DashboardController.onPageLoad()) // TODO CheckYourAnswers
  }

  override val checkRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(CheckMode)
    case DataTypesPage                  =>
      navigateTo(controllers.thirdparty.routes.DeclarationDateController.onPageLoad(CheckMode))
    case EoriNumberPage                 =>
      navigateTo(controllers.routes.DashboardController.onPageLoad())
    case ThirdPartyReferencePage        => thirdPartyReferenceRoutes(CheckMode)
    case ThirdPartyAccessStartDatePage  => accessStartDateRoutes(CheckMode)
    case DeclarationDatePage            => declarationDateRoutes(CheckMode)
    case DataStartDatePage              => dataStartDateRoutes(CheckMode)
    case DataEndDatePage                => navigateTo(controllers.routes.DashboardController.onPageLoad())

  }

  override val normalRoutesWithFlag: Page => UserAnswers => Boolean => Call = { case ConfirmEoriPage =>
    answers => skipFlag => confirmEoriPageRoutes(NormalMode, skipFlag)(answers)
  }

  override val checkRoutesWithFlag: Page => UserAnswers => Boolean => Call = { case ConfirmEoriPage =>
    answers => skipFlag => confirmEoriPageRoutes(CheckMode, skipFlag)(answers)
  }

  private def confirmEoriPageRoutes(mode: Mode, skipFlag: Boolean)(answers: UserAnswers): Call =
    answers.get(ConfirmEoriPage) match {
      case Some(ConfirmEori.Yes) if skipFlag =>
        controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(mode)

      case Some(ConfirmEori.Yes) if !skipFlag =>
        controllers.thirdparty.routes.ThirdPartyReferenceController.onPageLoad(mode)

      case Some(ConfirmEori.No) =>
        controllers.thirdparty.routes.EoriNumberController.onPageLoad(mode)

      case Some(_) =>
        controllers.problem.routes.JourneyRecoveryController.onPageLoad()

      case None =>
        controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigateTo(call: => Call): UserAnswers => Call = _ => call

  private def thirdPartyReferenceRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ThirdPartyReferencePage) match {
      case Some(_) =>
        mode match {
          case NormalMode => controllers.thirdparty.routes.ThirdPartyAccessStartDateController.onPageLoad(NormalMode)
          // CHANGE FOR CHECKMODE
          case CheckMode  => controllers.routes.DashboardController.onPageLoad()
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

  private def accessStartDateRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ThirdPartyAccessStartDatePage) match {
      case Some(_) =>
        mode match {
          case NormalMode => controllers.thirdparty.routes.ThirdPartyAccessEndDateController.onPageLoad(NormalMode)
          case CheckMode  => controllers.thirdparty.routes.ThirdPartyAccessEndDateController.onPageLoad(CheckMode)
        }
      case None    => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def declarationDateRoutes(mode: Mode)(answers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        answers
          .get(DeclarationDatePage)
          .map {
            case DeclarationDate.AllAvailableData =>
              controllers.routes.DashboardController.onPageLoad() // TODO with TRE-594
            case DeclarationDate.CustomDateRange  =>
              controllers.thirdparty.routes.DataStartDateController.onPageLoad(NormalMode)
          }
          .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

      case CheckMode =>
        controllers.routes.DashboardController.onPageLoad()
    }

  private def dataStartDateRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(DataStartDatePage) match {
      case Some(_) =>
        mode match {
          case NormalMode => controllers.thirdparty.routes.DataEndDateController.onPageLoad(NormalMode)
          case CheckMode  => controllers.thirdparty.routes.DataEndDateController.onPageLoad(CheckMode)
        }
      case None    => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }
}
