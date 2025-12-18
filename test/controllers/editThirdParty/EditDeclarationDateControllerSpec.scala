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

package controllers.editThirdParty

import base.SpecBase
import forms.editThirdParty.EditDeclarationDateFormProvider
import models.thirdparty.DeclarationDate
import models.{ThirdPartyDetails, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.editThirdParty.EditDeclarationDatePage
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.TradeReportingExtractsService
import views.html.editThirdParty.EditDeclarationDateView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class EditDeclarationDateControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider                  = new EditDeclarationDateFormProvider()
  private def form(dataTypesString: String) = formProvider(Seq(dataTypesString))

  def onwardRoute = Call("GET", "/foo")

  private val currentLocalDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

  private def thirdPartyDetails(dataStart: Option[LocalDate], dataEnd: Option[LocalDate], dataTypes: Set[String]) =
    ThirdPartyDetails(
      referenceName = Some("TestingReport"),
      accessStartDate = currentLocalDate,
      accessEndDate = None,
      dataTypes = dataTypes,
      dataStartDate = dataStart,
      dataEndDate = dataEnd
    )

  val thirdPartyEori = "GB123456789000"

  lazy val editRoute = controllers.editThirdParty.routes.EditDeclarationDateController.onPageLoad(thirdPartyEori).url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, editRoute)

  def postRequestAllAvailable(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, editRoute)
      .withFormUrlEncodedBody("value" -> DeclarationDate.AllAvailableData.toString)

  def postRequestCustomRange(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, editRoute)
      .withFormUrlEncodedBody("value" -> DeclarationDate.CustomDateRange.toString)

  "EditDeclarationDate Controller" - {

    "must return OK and the correct view for a GET when no prior data and service has no start date" in {
      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None, Set("imports"))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeService)
        )
        .build()

      running(application) {
        val view = application.injector.instanceOf[EditDeclarationDateView]

        val result = route(application, getRequest()).value

        val dataTypesString = messages(application)("declarationDate.import")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form(dataTypesString).fill(DeclarationDate.AllAvailableData),
          thirdPartyEori,
          dataTypesString
        )(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val saved       = DeclarationDate.CustomDateRange
      val userAnswers = UserAnswers(userAnswersId).set(EditDeclarationDatePage(thirdPartyEori), saved).success.value

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(Some(currentLocalDate), None, Set("imports"))))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TradeReportingExtractsService].toInstance(mockTradeService)
        )
        .build()

      running(application) {
        val view = application.injector.instanceOf[EditDeclarationDateView]

        val result = route(application, getRequest()).value

        val dataTypesString = messages(application)("declarationDate.import")

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form(dataTypesString).fill(saved), thirdPartyEori, dataTypesString)(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockTradeService = mock[TradeReportingExtractsService]
      when(mockTradeService.getThirdPartyDetails(any(), eqTo(thirdPartyEori))(any()))
        .thenReturn(Future.successful(thirdPartyDetails(None, None, Set("imports", "exports"))))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[TradeReportingExtractsService].toInstance(mockTradeService)
          )
          .build()

      running(application) {
        val result = route(application, postRequestAllAvailable()).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequestAllAvailable()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.problem.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
