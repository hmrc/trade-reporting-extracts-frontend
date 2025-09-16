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

package controllers.problem

import base.SpecBase
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.problem.NoDataFoundView

class NoDataFoundControllerSpec extends SpecBase {

  "NoDataFound Controller" - {

    "must return OK and the correct view for a GET" in {
      val reportName      = "Test Report"
      val reportRef       = "REF123"
      val reportStartDate = "2024-01-01"
      val reportEndDate   = "2024-12-31"

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(
          GET,
          controllers.problem.routes.NoDataFoundController
            .onPageLoad(reportName, reportRef, reportStartDate, reportEndDate)
            .url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoDataFoundView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          reportName,
          reportRef,
          reportStartDate,
          reportEndDate
        )(request, messages(application)).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include(
          messages(application)("noDataFound.heading", reportName, reportRef)
        )
        document.text()                                        must include(messages(application)("noDataFound.message1", reportStartDate, reportEndDate))
      }
    }
  }
}
