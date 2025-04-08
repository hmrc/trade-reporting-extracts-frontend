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

import base.SpecBase
import controllers.routes
import models.*
import pages.*
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

class NavigatorSpec extends SpecBase {

  val navigator = new FakeNavigation()

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }
    }

    "navigator journeyRecovery must redirect to journey recovery page" in {
      val continueUrl = Some(RedirectUrl("https://foo.com"))
      val result      = navigator.journeyRecovery(continueUrl)

      result mustBe Redirect(
        controllers.problem.routes.JourneyRecoveryController.onPageLoad(continueUrl)
      )
    }

    def checkNavigation(nextUrl: String, expectedUrl: String) = {
      val urlWithNoContext = nextUrl.replace("/public-pension-adjustment", "")
      urlWithNoContext mustBe expectedUrl
    }
  }

}
