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

package controllers.actions

import base.SpecBase
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import services.TradeReportingExtractsService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers.*

import scala.concurrent.{ExecutionContext, Future}

class BelowReportRequestLimitActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  val mockTradeReportingExtractsService: TradeReportingExtractsService = mock[TradeReportingExtractsService]

  class Harness(service: TradeReportingExtractsService)(implicit ec: ExecutionContext)
      extends BelowReportRequestLimitActionImpl(service) {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val dataRequest: DataRequest[AnyContent] = DataRequest(
    request = FakeRequest(),
    userId = "user-id-123",
    eori = "GB1234567890",
    affinityGroup = AffinityGroup.Individual,
    userAnswers = UserAnswers("user-id-123")
  )

  "BelowReportRequestLimitAction" - {

    "should allow the request to proceed when submission limit has not been reached" in {
      when(mockTradeReportingExtractsService.hasReachedSubmissionLimit(any())(any()))
        .thenReturn(Future.successful(false))

      val action = new Harness(mockTradeReportingExtractsService)

      val result = action.callRefine(dataRequest).futureValue

      result mustBe Right(dataRequest)
    }

    "should redirect to TooManySubmissionsController when submission limit is reached" in {
      when(mockTradeReportingExtractsService.hasReachedSubmissionLimit(any())(any()))
        .thenReturn(Future.successful(true))

      val action = new Harness(mockTradeReportingExtractsService)

      val result = action.callRefine(dataRequest).futureValue

      result mustBe a[Left[_, _]]
      val redirect = result.left.get
      redirect.header.status mustBe SEE_OTHER
      redirect.header
        .headers("Location") mustBe controllers.problem.routes.TooManySubmissionsController.onPageLoad().url
    }
  }
}
