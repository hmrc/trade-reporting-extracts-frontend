package controllers.report

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.*
import play.api.test.*
import views.html.report.ReportGuidanceView

class ReportGuidanceControllerSpec extends SpecBase with MockitoSugar {

  "ReportGuidanceController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReportGuidanceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReportGuidanceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
