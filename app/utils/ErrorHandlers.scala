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

import exceptions.NoAuthorisedUserFoundException
import models.AlreadySubmittedFlag
import models.report.ReportRequestSection
import models.requests.DataRequest
import pages.report.SelectThirdPartyEoriPage
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.Redirect
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

object ErrorHandlers {

  def handleNoAuthorisedUserFoundException(
    request: DataRequest[AnyContent],
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext): PartialFunction[Throwable, Future[Result]] = {
    case _: NoAuthorisedUserFoundException =>
      for {
        updatedAnswers                   <-
          Future.successful(ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers))
        updatedAnswersWithSubmissionFlag <- Future.fromTry(updatedAnswers.set(AlreadySubmittedFlag(), true))
        _                                <- sessionRepository.set(updatedAnswersWithSubmissionFlag)
      } yield request.userAnswers.get(SelectThirdPartyEoriPage) match {
        case Some(eori) => Redirect(controllers.report.routes.RequestNotCompletedController.onPageLoad(eori))
        case None       => Redirect(controllers.routes.IndexController.onPageLoad())
      }
  }

  def handleNoAuthorisedUserFoundException(
    request: DataRequest[AnyContent],
    sessionRepository: SessionRepository,
    thirdPartyEori: String
  )(implicit ec: ExecutionContext): PartialFunction[Throwable, Future[Result]] = {
    case _: NoAuthorisedUserFoundException =>
      for {
        updatedAnswers                   <-
          Future.successful(ReportRequestSection.removeAllReportRequestAnswersAndNavigation(request.userAnswers))
        updatedAnswersWithSubmissionFlag <- Future.fromTry(updatedAnswers.set(AlreadySubmittedFlag(), true))
        _                                <- sessionRepository.set(updatedAnswersWithSubmissionFlag)
      } yield Redirect(controllers.report.routes.RequestNotCompletedController.onPageLoad(thirdPartyEori))
  }
}
