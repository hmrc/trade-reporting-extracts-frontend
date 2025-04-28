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

import controllers.routes
import models.report.ReportDateRange
import models.{NormalMode, UserAnswers}
import models.report.EmailSelection
import pages.Page
import pages.report._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ReportNavigator @Inject() extends Navigator {

  override val normalRoutes: Page => UserAnswers => Call = {
    case DecisionPage               => navigateTo(controllers.report.routes.ChooseEoriController.onPageLoad(NormalMode))
    case ChooseEoriPage             => navigateTo(controllers.report.routes.EoriRoleController.onPageLoad(NormalMode))
    case EoriRolePage               => navigateTo(controllers.report.routes.ReportTypeImportController.onPageLoad(NormalMode))
    case ReportTypeImportPage       => navigateTo(controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode))
    case ReportDateRangePage        => reportDateRangePageNormalRoutes
    case CustomRequestStartDatePage =>
      navigateTo(controllers.report.routes.CustomRequestEndDateController.onPageLoad(NormalMode))
    case CustomRequestEndDatePage   => navigateTo(controllers.report.routes.ReportNameController.onPageLoad(NormalMode))
    case ReportNamePage             => navigateTo(controllers.report.routes.MaybeAdditionalEmailController.onPageLoad(NormalMode))
    case MaybeAdditionalEmailPage   =>
      conditionalNavigate(
        hasAdditionalEmailRequest,
        controllers.report.routes.EmailSelectionController.onPageLoad(NormalMode)
      )
    case EmailSelectionPage         =>
      conditionalNavigate(
        isAddNewEmail,
        controllers.report.routes.NewEmailNotificationController.onPageLoad(NormalMode)
      )
    case NewEmailNotificationPage   => navigateTo(controllers.report.routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersPage       => navigateTo(controllers.report.routes.ReportGuidanceController.onPageLoad())
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

  override val checkRoutes: Page => UserAnswers => Call = _ =>
    _ => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
}
