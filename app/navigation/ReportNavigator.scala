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

import config.FrontendAppConfig
import models.report.{ChooseEori, Decision, EmailSelection, ReportDateRange}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.report.*
import pages.thirdparty.ConfirmEoriPage
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ReportNavigator @Inject() (appConfig: FrontendAppConfig) extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case DecisionPage                           =>
      navigateBasedOnThirdPartyFlag(
        controllers.report.routes.ChooseEoriController.onPageLoad(NormalMode),
        controllers.report.routes.EoriRoleController.onPageLoad(NormalMode)
      )
    case ChooseEoriPage                         => chooseEoriRoutes(NormalMode)
    case AccountsYouHaveAuthorityOverImportPage => accountsYouHaveAuthorityOverImportRoutes(NormalMode)
    case EoriRolePage                           => eoriRoleRoutes(NormalMode)
    case ReportTypeImportPage                   => navigateTo(controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode))
    case ReportDateRangePage                    => reportDateRangeRoutes(NormalMode)
    case CustomRequestStartDatePage             =>
      navigateTo(controllers.report.routes.CustomRequestEndDateController.onPageLoad(NormalMode))
    case CustomRequestEndDatePage               => navigateTo(controllers.report.routes.ReportNameController.onPageLoad(NormalMode))

    case ReportNamePage =>
      navigateBasedOnNotificationsFlag(
        controllers.report.routes.MaybeAdditionalEmailController.onPageLoad(NormalMode),
        controllers.report.routes.CheckYourAnswersController.onPageLoad()
      )

    case MaybeAdditionalEmailPage =>
      conditionalNavigate(
        hasAdditionalEmailRequest,
        controllers.report.routes.EmailSelectionController.onPageLoad(NormalMode)
      )
    case EmailSelectionPage       =>
      conditionalNavigate(
        isAddNewEmail,
        controllers.report.routes.NewEmailNotificationController.onPageLoad(NormalMode)
      )
    case NewEmailNotificationPage => navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersPage     => navigateTo(controllers.report.routes.RequestConfirmationController.onPageLoad())
  }

  override val checkRoutes: Page => UserAnswers => Call = {
    case DecisionPage                           =>
      navigateTo(controllers.report.routes.EoriRoleController.onPageLoad(CheckMode))
    case ChooseEoriPage                         => chooseEoriRoutes(CheckMode)
    case AccountsYouHaveAuthorityOverImportPage => accountsYouHaveAuthorityOverImportRoutes(CheckMode)
    case EoriRolePage                           => eoriRoleRoutes(CheckMode)
    case ReportTypeImportPage                   => navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
    case ReportDateRangePage                    => reportDateRangeRoutes(CheckMode)
    case CustomRequestStartDatePage             =>
      navigateTo(controllers.report.routes.CustomRequestEndDateController.onPageLoad(CheckMode))
    case CustomRequestEndDatePage               => navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
    case ReportNamePage                         =>
      navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
    case MaybeAdditionalEmailPage               =>
      conditionalNavigate(
        hasAdditionalEmailRequest,
        controllers.report.routes.EmailSelectionController.onPageLoad(CheckMode)
      )
    case EmailSelectionPage                     =>
      conditionalNavigate(
        isAddNewEmail,
        controllers.report.routes.NewEmailNotificationController.onPageLoad(CheckMode)
      )
    case NewEmailNotificationPage               => navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
    case _                                      => _ => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
  }

  private def navigateTo(call: => Call): UserAnswers => Call = _ => call

  private def conditionalNavigate(condition: UserAnswers => Boolean, successCall: => Call): UserAnswers => Call =
    answers =>
      if (condition(answers)) successCall else controllers.report.routes.CheckYourAnswersController.onPageLoad()

  private def hasAdditionalEmailRequest(answers: UserAnswers): Boolean =
    answers.get(MaybeAdditionalEmailPage).getOrElse(false)

  private def isAddNewEmail(answers: UserAnswers): Boolean =
    answers.get(EmailSelectionPage).exists(_.contains(EmailSelection.AddNewEmailValue))

  private def reportDateRangeRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ReportDateRangePage) match {
      case Some(ReportDateRange.CustomDateRange) =>
        controllers.report.routes.CustomRequestStartDateController.onPageLoad(mode)

      case Some(_) =>
        mode match {
          case NormalMode =>
            controllers.report.routes.ReportNameController.onPageLoad(NormalMode)

          case CheckMode =>
            controllers.report.routes.CheckYourAnswersController.onPageLoad()
        }

      case None =>
        controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def chooseEoriRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(ChooseEoriPage) match {
      case Some(ChooseEori.Myeori) =>
        mode match {
          case NormalMode =>
            controllers.report.routes.EoriRoleController.onPageLoad(NormalMode)
          case CheckMode  =>
            controllers.report.routes.CheckYourAnswersController.onPageLoad()
        }

      case Some(ChooseEori.Myauthority) =>
        controllers.report.routes.AccountsYouHaveAuthorityOverImportController.onPageLoad(mode)

      case None =>
        controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def accountsYouHaveAuthorityOverImportRoutes(mode: Mode)(answers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        answers
          .get(DecisionPage)
          .map {
            case Decision.Import => controllers.report.routes.ReportTypeImportController.onPageLoad(NormalMode)
            case Decision.Export => controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode)
          }
          .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

      case CheckMode =>
        controllers.report.routes.CheckYourAnswersController.onPageLoad()
    }

  private def eoriRoleRoutes(mode: Mode)(answers: UserAnswers): Call =
    answers.get(DecisionPage) match {
      case Some(Decision.Import) =>
        controllers.report.routes.ReportTypeImportController.onPageLoad(mode)

      case Some(Decision.Export) =>
        mode match {
          case NormalMode => controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode)
          case CheckMode  => controllers.report.routes.CheckYourAnswersController.onPageLoad()
        }

      case None =>
        controllers.problem.routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigateBasedOnThirdPartyFlag(
    ifThirdPartyEnabled: => Call,
    ifThirdPartyDisabled: => Call
  ): UserAnswers => Call = { _ =>
    if (appConfig.thirdPartyEnabled) ifThirdPartyEnabled else ifThirdPartyDisabled
  }

  private def navigateBasedOnNotificationsFlag(
    ifNotificationsEnabled: => Call,
    ifNotificationsDisabled: => Call
  ): UserAnswers => Call = { _ =>
    if (appConfig.notificationsEnabled) ifNotificationsEnabled else ifNotificationsDisabled
  }

  override val normalRoutesWithFlag: Page => UserAnswers => Boolean => Call = _ =>
    answers => skipFlag => controllers.problem.routes.JourneyRecoveryController.onPageLoad()

  override val checkRoutesWithFlag: Page => UserAnswers => Boolean => Call = _ =>
    answers => skipFlag => controllers.problem.routes.JourneyRecoveryController.onPageLoad()

}
