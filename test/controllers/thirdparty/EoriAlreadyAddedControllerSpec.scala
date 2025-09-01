package controllers.thirdparty

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.thirdparty.EoriAlreadyAddedView

class EoriAlreadyAddedControllerSpec extends SpecBase {

  "EoriAlreadyAdded Controller" - {

    "must return OK and the correct view for a GET with flash data" in {

      val eori = "GB123456789000"

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.EoriAlreadyAddedController.onPageLoad().url)
          .withFlash("alreadyAddedEori" -> eori)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EoriAlreadyAddedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(eori)(request, messages(application)).toString
      }
    }
  }
}

