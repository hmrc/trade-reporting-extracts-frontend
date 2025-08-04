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

package controllers

import controllers.actions.*
import play.api.http.HttpEntity
import play.api.i18n.MessagesApi
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, ResponseHeader, Result}
import services.TradeReportingExtractsService
import views.html.AvailableReportsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AvailableReportsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  ws: WSClient,
  val controllerComponents: MessagesControllerComponents,
  view: AvailableReportsView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends BaseController {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    for {
      availableReports      <- tradeReportingExtractsService.getAvailableReports(request.eori)
      maybeUserReports       = availableReports.availableUserReports.exists(_.nonEmpty)
      maybeThirdPartyReports = availableReports.availableThirdPartyReports.isDefined
    } yield Ok(view(availableReports, maybeUserReports, maybeThirdPartyReports))
  }

  def auditDownloadFile(file: String, fileName: String, reportReference: String): Action[AnyContent] = Action.async {
    implicit request =>
      ws.url(file).stream().map { response =>
        val downloadResponse = downloadFileResponse(fileName, response)
        tradeReportingExtractsService
          .auditReportDownload(
            reportReference,
            fileName,
            file
          )
          .foreach(_ => ())
        downloadResponse
      }
  }

  private def downloadFileResponse(
    fileName: String,
    response: play.api.libs.ws.StandaloneWSResponse
  ): Result =
    Result(
      header = ResponseHeader(
        OK,
        Map(
          "Content-Disposition" -> s"attachment; filename=$fileName",
          "Content-Type"        -> response.contentType
        )
      ),
      body = HttpEntity.Streamed(
        data = response.bodyAsSource,
        contentLength = response.headers.get("Content-Length").flatMap(_.headOption).map(_.toLong),
        contentType = Some(response.contentType)
      )
    )
}
