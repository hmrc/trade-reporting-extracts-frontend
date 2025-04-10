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

import models.{NormalMode, UserAnswers}
import pages.Page
import pages.report.{ChooseEoriPage, DecisionPage, EoriRolePage, ReportDateRangePage, ReportNamePage, ReportTypeImportPage}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ReportNavigator @Inject() extends Navigator {
  override val normalRoutes: Page => UserAnswers => Call = {
    case DecisionPage         =>
      _ => controllers.report.routes.ChooseEoriController.onPageLoad(NormalMode)
    case ChooseEoriPage       =>
      _ => controllers.report.routes.EoriRoleController.onPageLoad(NormalMode)
    case EoriRolePage         =>
      _ => controllers.report.routes.ReportTypeImportController.onPageLoad(NormalMode)
    case ReportTypeImportPage =>
      _ => controllers.report.routes.ReportDateRangeController.onPageLoad(NormalMode)
    case ReportDateRangePage  =>
      _ => controllers.report.routes.ReportNameController.onPageLoad(NormalMode)
    case ReportNamePage       =>
      _ => controllers.report.routes.MaybeAdditionalEmailController.onPageLoad(NormalMode)
  }
  override val checkRoutes: Page => UserAnswers => Call  = _ =>
    _ => controllers.problem.routes.JourneyRecoveryController.onPageLoad()
}
