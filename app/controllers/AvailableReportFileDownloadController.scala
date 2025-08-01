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

import com.google.inject.Inject
import forms.AvailableReportDownloadFormProvider
import play.api.http.HttpEntity
import play.api.i18n.MessagesApi
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, ResponseHeader, Result}
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import utils.ReportHelpers

import scala.concurrent.{ExecutionContext, Future}

class AvailableReportFileDownloadController @Inject() (
  override val messagesApi: MessagesApi,
  ws: WSClient,
  auditService: AuditService,
  formProvider: AvailableReportDownloadFormProvider,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController {

  def availableReportDownloadFile(): Action[AnyContent] = Action.async { implicit request =>
    formProvider()
      .bindFromRequest()
      .fold(
        _ => Future.successful(BadRequest("Error processing request")),
        formData => {
          auditReportDownload(formData)
          ws.url(formData.fileURL).stream().map { response =>
            downloadFileResponse(formData.fileName, response)
          }
        }
      )
  }

  private def auditReportDownload(formData: models.availableReports.AvailableReportDownload)(implicit
    hc: HeaderCarrier
  ): Unit =
    auditService.audit(
      models.audit.ReportRequestDownloadedAudit(
        requestId = formData.referenceNumber,
        totalReportParts = formData.reportFilesParts,
        reportTypeName = formData.reportType,
        fileUrl = formData.fileURL,
        fileName = formData.fileName,
        fileSizeBytes = ReportHelpers.formatBytes(formData.fileSize),
        reportSubjectEori = formData.reportSubjectEori,
        requesterEori = formData.requesterEORI
      )
    )

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
