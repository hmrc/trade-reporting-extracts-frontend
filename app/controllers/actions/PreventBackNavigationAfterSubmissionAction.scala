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

import models.AlreadySubmittedFlag
import models.requests.DataRequest
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreventBackNavigationAfterSubmissionImpl @Inject() (implicit val executionContext: ExecutionContext)
    extends PreventBackNavigationAfterSubmissionAction {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    val previouslySubmitted = request.userAnswers.get(AlreadySubmittedFlag()).getOrElse(false)
    if (previouslySubmitted) {
      Future.successful(Left(Redirect(controllers.problem.routes.AlreadySubmittedController.onPageLoad())))
    } else {
      Future.successful(
        Right(DataRequest(request.request, request.userId, request.eori, request.affinityGroup, request.userAnswers))
      )
    }
  }
}

trait PreventBackNavigationAfterSubmissionAction extends ActionRefiner[DataRequest, DataRequest]
