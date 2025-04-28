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

package controllers.report

import controllers.BaseController
import controllers.actions.*
import models.report.EmailSelection
import models.requests.DataRequest
import pages.report.{EmailSelectionPage, NewEmailNotificationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import views.html.report.RequestConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequestConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RequestConfirmationView
)(implicit ec: ExecutionContext)
    extends BaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val updatedList: Seq[String] = fetchUpdatedData(request)
    Future.successful(Ok(view(updatedList)))
  }

  private def fetchUpdatedData(request: DataRequest[AnyContent]): Seq[String] =
    request.userAnswers.get(EmailSelectionPage).toSeq.flatMap { answer =>
      answer.map {
        case EmailSelection.Email3 =>
          request.userAnswers
            .get(NewEmailNotificationPage)
            .map(HtmlFormat.escape(_).toString)
            .getOrElse("")
        case email                 =>
          HtmlFormat.escape(s"emailSelection.$email").toString
      }
    }
}
