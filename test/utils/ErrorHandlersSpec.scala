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

package utils

import base.SpecBase
import exceptions.NoAuthorisedUserFoundException
import models.requests.DataRequest
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.report.SelectThirdPartyEoriPage
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.{ExecutionContext, Future}

class ErrorHandlersSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global


  "ErrorHandlers" - {

    "handleNoAuthorisedUserFoundException" - {

      val mockSessionRepository = mock[SessionRepository]
      val testEori = "GB123456789012"
      val userAnswers = emptyUserAnswers.set(SelectThirdPartyEoriPage, testEori).success.value

      val dataRequest: DataRequest[AnyContent] = DataRequest(
        request = FakeRequest(),
        userId = "user-id-123",
        eori = "GB1234567890",
        affinityGroup = AffinityGroup.Individual,
        userAnswers = userAnswers
      )

      "must redirect to RequestNotCompletedController with EORI when SelectThirdPartyEoriPage has a value" in {
        reset(mockSessionRepository)
        when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequest, mockSessionRepository)
        val exception = new NoAuthorisedUserFoundException("Test exception")

        val result = handler(exception).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)) mustBe Some(controllers.report.routes.RequestNotCompletedController.onPageLoad(testEori).url)

        verify(mockSessionRepository, times(1)).set(any[UserAnswers]())
      }

      "must redirect to IndexController when SelectThirdPartyEoriPage has no value" in {
        reset(mockSessionRepository)
        val userAnswers = emptyUserAnswers
        val dataRequestWithNewAnswers = dataRequest.copy(userAnswers = userAnswers)
        when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequestWithNewAnswers, mockSessionRepository)
        val exception = new NoAuthorisedUserFoundException("Test exception")

        val result = handler(exception).futureValue

        status(Future.successful(result)) mustEqual SEE_OTHER
        redirectLocation(Future.successful(result)) mustBe Some(controllers.routes.IndexController.onPageLoad().url)

        verify(mockSessionRepository, times(1)).set(any[UserAnswers]())
      }

      "must set AlreadySubmittedFlag to true in session" in {
        reset(mockSessionRepository)
        val userAnswers = emptyUserAnswers.set(SelectThirdPartyEoriPage, testEori).success.value

        when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequest, mockSessionRepository)
        val exception = new NoAuthorisedUserFoundException("Test exception")

        handler(exception).futureValue

        verify(mockSessionRepository, times(1)).set(any[UserAnswers]())
      }

      "must clear report request answers and navigation" in {
        reset(mockSessionRepository)
        val userAnswers = emptyUserAnswers.set(SelectThirdPartyEoriPage, testEori).success.value

        when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.successful(true))

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequest, mockSessionRepository)
        val exception = new NoAuthorisedUserFoundException("Test exception")

        handler(exception).futureValue

        verify(mockSessionRepository, times(1)).set(any[UserAnswers]())
      }

      "must handle session repository failure gracefully" in {
        val userAnswers = emptyUserAnswers.set(SelectThirdPartyEoriPage, testEori).success.value

        when(mockSessionRepository.set(any[UserAnswers]())).thenReturn(Future.failed(new RuntimeException("Session save failed")))

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequest, mockSessionRepository)
        val exception = new NoAuthorisedUserFoundException("Test exception")

        val f = handler(exception)
        f.failed.futureValue.getMessage mustBe "Session save failed"
      }

      "must only handle NoAuthorisedUserFoundException" in {
        val userAnswers = emptyUserAnswers

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequest, mockSessionRepository)
        val otherException = new RuntimeException("Different exception")

        handler.isDefinedAt(otherException) mustBe false
      }

      "must be defined for NoAuthorisedUserFoundException" in {
        val userAnswers = emptyUserAnswers

        val handler = ErrorHandlers.handleNoAuthorisedUserFoundException(dataRequest, mockSessionRepository)
        val targetException = new NoAuthorisedUserFoundException("Target exception")

        handler.isDefinedAt(targetException) mustBe true
      }
    }
  }
}