package controllers.thirdparty

import base.SpecBase
import models.thirdparty.AuthorisedThirdPartiesViewModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TradeReportingExtractsService
import views.html.thirdparty.AuthorisedThirdPartiesView

import scala.concurrent.Future

class AuthorisedThirdPartiesControllerSpec extends SpecBase with MockitoSugar {

  "AuthorisedThirdPartiesController" - {

    "must return OK and the correct view for a GET when no authorised third parties" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      when(mockTradeReportingExtractsService.getAuthorisedThirdParties(any())(any()))
        .thenReturn(Future.successful(Seq.empty[AuthorisedThirdPartiesViewModel]))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad().url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedThirdPartiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(
          request,
          messages(application)
        ).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include("You have not added any third parties yet")
      }
    }

    "must return OK and the correct view for a GET when authorised third parties exist" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      val thirdParty = AuthorisedThirdPartiesViewModel(
        eori = "GB123456789000",
        businessInfo = Some("Business Name"),
        referenceName = Some("Reference Name")
      )
      when(mockTradeReportingExtractsService.getAuthorisedThirdParties(any())(any()))
        .thenReturn(Future.successful(Seq(thirdParty)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad().url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedThirdPartiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq(thirdParty))(
          request,
          messages(application)
        ).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include("Manage third parties that can access your data")
        document.text() must include("GB123456789000")
        document.text() must include("Business Name")
        document.text() must include("Reference Name")
      }
    }

    "must return OK and the correct view for a GET when authorised third parties exist but show correctly when businessInfo and refName don't exist" in {
      val mockTradeReportingExtractsService = mock[TradeReportingExtractsService]
      val thirdParty = AuthorisedThirdPartiesViewModel(
        eori = "GB123456789000",
        businessInfo = None,
        referenceName = None
      )
      when(mockTradeReportingExtractsService.getAuthorisedThirdParties(any())(any()))
        .thenReturn(Future.successful(Seq(thirdParty)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TradeReportingExtractsService].toInstance(mockTradeReportingExtractsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad().url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[AuthorisedThirdPartiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq(thirdParty))(
          request,
          messages(application)
        ).toString

        val document = Jsoup.parse(contentAsString(result))
        document.getElementsByClass("govuk-heading-xl").text() must include("Manage third parties that can access your data")
        document.text() must include("GB123456789000")
        document.text() must include("This business has not agreed to share their data. Contact them directly for more information.")
        document.text() must include("Not applicable")
      }
    }
  }
}