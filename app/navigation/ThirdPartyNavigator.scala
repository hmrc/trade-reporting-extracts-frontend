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
import utils.json.OptionalLocalDateReads.*

class ThirdPartyNavigator @Inject() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage    =>
      dataOwnerConsentRoutes(NormalMode)
    case EoriNumberPage                    =>
      eoriNumberRoutes(NormalMode)
    case ThirdPartyReferencePage           => thirdPartyReferenceRoutes(NormalMode)
    case ThirdPartyAccessStartDatePage     => accessStartDateRoutes(NormalMode)
    case ThirdPartyAccessEndDatePage       => accessEndDateRoutes(NormalMode)
    case DataTypesPage                     =>
      dataTypesRoutes(NormalMode)
    case DeclarationDatePage               => declarationDateRoutes(NormalMode)
    case DataStartDatePage                 => dataStartDateRoutes(NormalMode)
    case DataEndDatePage                   =>
      navigateTo(controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad())
    case AddThirdPartyCheckYourAnswersPage =>
      navigateTo(controllers.thirdparty.routes.ThirdPartyAddedConfirmationController.onPageLoad())
  }

  override val checkRoutes: Page => UserAnswers => Call = {
    case ThirdPartyDataOwnerConsentPage =>
      dataOwnerConsentRoutes(CheckMode)
    case EoriNumberPage                 =>
      eoriNumberRoutes(CheckMode)
    case ThirdPartyReferencePage        => thirdPartyReferenceRoutes(CheckMode)
    case ThirdPartyAccessStartDatePage  => accessStartDateRoutes(CheckMode)
    case ThirdPartyAccessEndDatePage    => accessEndDateRoutes(CheckMode)
    case DataTypesPage                  =>
      dataTypesRoutes(CheckMode)
    case DeclarationDatePage            => declarationDateRoutes(CheckMode)
    case DataStartDatePage              => dataStartDateRoutes(CheckMode)
    case DataEndDatePage                =>
      navigateTo(controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad())
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
          case CheckMode  => controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
        }
      case None    => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def dataOwnerConsentRoutes(mode: Mode)(answers: UserAnswers): Call = {
    mode match {
      case NormalMode =>     answers.get(ThirdPartyDataOwnerConsentPage) match {
        case Some(true)  =>
          controllers.thirdparty.routes.EoriNumberController.onPageLoad(mode)
        case Some(false) =>
          controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        case None        => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
      }
      case CheckMode  => answers.get(ThirdPartyDataOwnerConsentPage) match {
        case Some(true)  => answers.get(EoriNumberPage) match {
          case Some(_) => controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
          case None    =>  controllers.thirdparty.routes.EoriNumberController.onPageLoad(mode)
        }
        case Some(false) =>
          controllers.thirdparty.routes.CannotAddThirdPartyController.onPageLoad()
        case None        => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
      }
    }

  }

  private def eoriNumberRoutes(mode: Mode)(answers: UserAnswers): Call = {
    mode match {
      case NormalMode => answers.get(EoriNumberPage) match {
        case Some(_) =>
          controllers.thirdparty.routes.ConfirmEoriController.onPageLoad(NormalMode)
        case _ => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
      }
      case CheckMode => answers.get(EoriNumberPage) match {
        case Some(_) => answers.get(ConfirmEoriPage) match {
          case Some(_) =>
            controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
          case _       =>
            controllers.thirdparty.routes.ConfirmEoriController.onPageLoad(NormalMode)
        }
        case _ => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
      }
    }

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

  private def accessEndDateRoutes(mode: Mode)(answers: UserAnswers): Call =
    mode match {
      case NormalMode => controllers.thirdparty.routes.DataTypesController.onPageLoad(NormalMode)
      case CheckMode  =>
        answers
          .get(DataTypesPage) match {
          case Some(_) =>
            controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
          case None    =>
            controllers.thirdparty.routes.DataTypesController.onPageLoad(CheckMode)
        }
    }

  private def dataTypesRoutes(mode: Mode)(answers: UserAnswers): Call =
    mode match {
      case NormalMode => controllers.thirdparty.routes.DeclarationDateController.onPageLoad(NormalMode)
      case CheckMode  =>
        answers
          .get(DeclarationDatePage) match {
          case Some(_) =>
            controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
          case None    =>
            controllers.thirdparty.routes.DeclarationDateController.onPageLoad(CheckMode)
        }
    }

  private def declarationDateRoutes(mode: Mode)(answers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        answers
          .get(DeclarationDatePage)
          .map {
            case DeclarationDate.AllAvailableData =>
              controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
            case DeclarationDate.CustomDateRange  =>
              controllers.thirdparty.routes.DataStartDateController.onPageLoad(NormalMode)
          }
          .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())
      case CheckMode  => 
        answers
          .get(DeclarationDatePage)
          .map {
            case DeclarationDate.AllAvailableData =>
              controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
            case DeclarationDate.CustomDateRange  =>
              answers.get(DataStartDatePage) match {
                case Some(_) =>
                  controllers.thirdparty.routes.AddThirdPartyCheckYourAnswersController.onPageLoad()
                case _       =>
                  controllers.thirdparty.routes.DataStartDateController.onPageLoad(NormalMode)
              }
          }
          .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())
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
