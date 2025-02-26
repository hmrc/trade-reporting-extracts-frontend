package controllers

import base.SpecBase
import models.{AddressInformation, CompanyInformation}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{Application, inject}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import views.html.ContactDetailsView

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class ContactDetailsControllerSpec extends SpecBase {

  "ContactDetails Controller" - {

    "must return OK and the correct view for a GET" in new Setup {

      running(application) {
        when(mockApiService.getCompanyInformation(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(companyInformation))

        val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(companyInformation, eori)(request, messages(application)).toString
      }
    }
  }

  trait Setup {
    val mockApiService: TradeReportingExtractsService = mock[TradeReportingExtractsService]
    val companyInformation: CompanyInformation = CompanyInformation("ABC Company", "1",
      AddressInformation("XYZ Street", "ABC City", Some("G11 2ZZ"), "GB"))
    val eori = "GB123456789002"
    val application: Application = applicationBuilder()
      .overrides(
        inject.bind[TradeReportingExtractsService].toInstance(mockApiService)
      )
      .configure("features.new-agent-view-enabled" -> false)
      .build()
  }
}
