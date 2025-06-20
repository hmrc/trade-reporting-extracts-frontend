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

import models.report.{ChooseEori, Decision, EmailSelection, ReportDateRange}
import models.{NormalMode, UserAnswers}
import pages.Page
import pages.report.*
import play.api.mvc.Call
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}

@Singleton
class ReportNavigator @Inject() (appConfig: FrontendAppConfig) extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case DecisionPage                           => checkFlagForThirdPartyJourney
    case ChooseEoriPage                         => ChooseEoriNormalRoutes
    case AccountsYouHaveAuthorityOverImportPage => AccountsYouHaveAuthorityOverImportNormalRoutes
    case EoriRolePage                           => EoriRoleNormalRoutes
    case ReportTypeImportPage                   => navigateTo(controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode))
    case ReportDateRangePage                    => reportDateRangePageNormalRoutes
    case CustomRequestStartDatePage             =>
      navigateTo(controllers.report.routes.CustomRequestEndDateController.onPageLoad(NormalMode))
    case CustomRequestEndDatePage               => navigateTo(controllers.report.routes.ReportNameController.onPageLoad(NormalMode))

    case ReportNamePage =>
      if (appConfig.notificationsEnabled) {
        navigateTo(controllers.report.routes.MaybeAdditionalEmailController.onPageLoad(NormalMode))
      } else {
        navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
      }

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
    case CheckYourAnswersPage     => navigateTo(controllers.report.routes.RequestReportWaitingRoomController.onPageLoad())
  }

  private def navigateTo(call: => Call): UserAnswers => Call = _ => call

  private def conditionalNavigate(condition: UserAnswers => Boolean, successCall: => Call): UserAnswers => Call =
    answers =>
      if (condition(answers)) successCall else controllers.report.routes.CheckYourAnswersController.onPageLoad()

  private def hasAdditionalEmailRequest(answers: UserAnswers): Boolean =
    answers.get(MaybeAdditionalEmailPage).getOrElse(false)

  private def isAddNewEmail(answers: UserAnswers): Boolean =
    answers.get(EmailSelectionPage).exists(_.contains(EmailSelection.Email3))

  private def reportDateRangePageNormalRoutes(answers: UserAnswers): Call =
    answers
      .get(ReportDateRangePage)
      .map {
        case ReportDateRange.CustomDateRange =>
          controllers.report.routes.CustomRequestStartDateController.onPageLoad(NormalMode)
        case _                               => controllers.report.routes.ReportNameController.onPageLoad(NormalMode)
      }
      .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

  private def ChooseEoriNormalRoutes(answers: UserAnswers): Call =
    answers
      .get(ChooseEoriPage)
      .map {
        case ChooseEori.Myeori      => controllers.report.routes.EoriRoleController.onPageLoad(NormalMode)
        case ChooseEori.Myauthority =>
          controllers.report.routes.AccountsYouHaveAuthorityOverImportController.onPageLoad(NormalMode)
      }
      .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

  private def AccountsYouHaveAuthorityOverImportNormalRoutes(answers: UserAnswers): Call =
    answers
      .get(DecisionPage)
      .map {
        case Decision.Import => controllers.report.routes.ReportTypeImportController.onPageLoad(NormalMode)
        case Decision.Export => controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode)
      }
      .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

  private def EoriRoleNormalRoutes(answers: UserAnswers): Call =
    answers
      .get(DecisionPage)
      .map {
        case Decision.Import => controllers.report.routes.ReportTypeImportController.onPageLoad(NormalMode)
        case Decision.Export => controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode)
      }
      .getOrElse(controllers.problem.routes.JourneyRecoveryController.onPageLoad())

  private def checkFlagForThirdPartyJourney(answers: UserAnswers): Call =
    if (appConfig.thirdPartyEnabled)
      navigateTo(controllers.report.routes.ChooseEoriController.onPageLoad(NormalMode))(answers)
    else
      navigateTo(controllers.report.routes.EoriRoleController.onPageLoad(NormalMode))(answers)

  override val checkRoutes: Page => UserAnswers => Call = _ =>
    _ => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
}
