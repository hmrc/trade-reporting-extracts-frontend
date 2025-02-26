package controllers

import base.SpecBase
import models.{AddressInformation, CompanyInformation}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ContactDetailsView

class ContactDetailsControllerSpec extends SpecBase {

  "ContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CompanyInformation("ABC Company", "1",
          AddressInformation("XYZ Street", "ABC City", Some("G11 2ZZ"), "GB")), "GB123456789002")(request, messages(application)).toString
      }
    }
  }
}
