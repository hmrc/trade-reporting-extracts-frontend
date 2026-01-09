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

package controllers.thirdparty

import controllers.actions.*
import models.thirdparty.ThirdPartyRemovalMeta
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import views.html.thirdparty.ThirdPartyAccessSelfRemovedView

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ThirdPartyAccessSelfRemovedController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyAccessSelfRemovedView,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val submissionMeta = request.userAnswers.submissionMeta
        .map(_.as[ThirdPartyRemovalMeta])
        .getOrElse(ThirdPartyRemovalMeta("", Instant.now(), None))

      val (submittedDate, submittedTime) =
        DateTimeFormats.instantToDateAndTime(submissionMeta.submittedAt, clock)

      Future.successful(Ok(view(submittedDate, submittedTime, submissionMeta.eori)))
    }
}
