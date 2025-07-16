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

import javax.inject.Inject
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.TradeReportingExtractsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class BelowReportRequestLimitActionImpl @Inject() (
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit val executionContext: ExecutionContext)
    extends BelowReportRequestLimitAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    tradeReportingExtractsService.hasReachedSubmissionLimit(request.eori).map { reached =>
      if (!reached) {
        Right(DataRequest(request.request, request.userId, request.eori, request.affinityGroup, request.userAnswers))
      } else {
        Left(Redirect(controllers.problem.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
  }
}

trait BelowReportRequestLimitAction extends ActionRefiner[DataRequest, DataRequest]
