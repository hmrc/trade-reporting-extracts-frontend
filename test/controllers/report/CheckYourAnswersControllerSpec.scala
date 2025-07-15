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

package controllers.report

import base.SpecBase
import controllers.report
import models.SectionNavigation
import navigation.ReportNavigator
import pages.report.CheckYourAnswersPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.report.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase {

  "CheckYourAnswers Controller" - {
    val sectionNav = SectionNavigation("reportRequestSection")

    "must return OK and the correct view for a GET" in {
      val userAnswers =
        emptyUserAnswers.set(sectionNav, "/request-customs-declaration-data/check-your-answers").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, report.routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must redirect to the next page for a POST" in {
      val userAnswers =
        emptyUserAnswers.set(sectionNav, "/request-customs-declaration-data/check-your-answers").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, report.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.report.routes.RequestConfirmationController
          .onPageLoad()
          .url
      }
    }
  }
}
