/*
 * Copyright 2024 HM Revenue & Customs
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

import models.*
import pages.*
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Result}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

trait Navigator {

  val normalRoutes: Page => UserAnswers => Call
  val checkRoutes: Page => UserAnswers => Call

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRoutes(page)(userAnswers)
  }
  def journeyRecovery(continueUrl: Option[RedirectUrl] = None): Result = Redirect(
    controllers.problem.routes.JourneyRecoveryController.onPageLoad(continueUrl)
  )
}
