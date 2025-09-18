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
import models.AlreadyAddedThirdPartyFlag
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.{ExecutionContext, Future}

class PreventBackNavigationAfterAddThirdPartyActionSpec extends SpecBase with ScalaFutures with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  class TestablePreventBackNavigationAfterAddThirdPartyActionImpl(implicit ec: ExecutionContext)
    extends PreventBackNavigationAfterAddThirdPartyActionImpl {
    def publicRefine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val action  = new TestablePreventBackNavigationAfterAddThirdPartyActionImpl()
  val request = mock[Request[AnyContent]]

  "PreventBackNavigationAfterAddThirdPartyActionImpl" - {
    "should redirect if AlreadyAddedThirdPartyFlag is true" in {
      val userAnswers = emptyUserAnswers.set(AlreadyAddedThirdPartyFlag(), true).get
      val dataRequest = DataRequest(request, "userId", "eori", AffinityGroup.Individual, userAnswers)

      whenReady(action.publicRefine(dataRequest)) {
        case Left(result) =>
          result.header.headers("Location") should include("/already-added-third-party")
        case Right(_)     => fail("Should redirect")
      }
    }

    "should allow navigation if AlreadyAddedThirdPartyFlag is false" in {
      val userAnswers = emptyUserAnswers.set(AlreadyAddedThirdPartyFlag(), false).get
      val dataRequest = DataRequest(request, "userId", "eori", AffinityGroup.Individual, userAnswers)

      whenReady(action.publicRefine(dataRequest)) {
        case Right(result) =>
          result shouldBe dataRequest
        case Left(_)       => fail("Should not redirect")
      }
    }
  }
}