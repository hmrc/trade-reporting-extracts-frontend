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
import models.report.ReportConfirmationModel
import play.api.cache.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.report.RequestConfirmationView
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RequestConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  cache:              AsyncCacheApi,
  val controllerComponents: MessagesControllerComponents,
  view: RequestConfirmationView
)(implicit ec: ExecutionContext)
    extends BaseController
    with I18nSupport {

  def onPageLoad(key: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    cache.get[ReportConfirmationModel](key).map { reportConfirmationModelOpt =>
      val (updatedList, isMoreThanOneReport, requestRef) = reportConfirmationModelOpt match {
        case Some(reportConfirmationModel) =>
          (
            reportConfirmationModel.updatedList,
            reportConfirmationModel.isMoreThanOneReport,
            reportConfirmationModel.requestRef
          )
        case None =>
          (Seq.empty, false, "")
      }
      Ok(view(updatedList, isMoreThanOneReport, requestRef))
    }
  }

}
