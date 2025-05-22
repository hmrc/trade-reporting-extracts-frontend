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
import play.api.http.HttpEntity
import play.api.libs.ws.WSClient
import play.api.mvc.*
import org.apache.pekko.stream.scaladsl.{Source, StreamConverters}
import org.apache.pekko.util.ByteString
import play.api.libs.json.{Json, Reads}

import java.io.{ByteArrayOutputStream, OutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.Inject
import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*


class FileDownloadController @Inject() (
  ws: WSClient,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController {

  def downloadFile(): Action[AnyContent] = Action.async { implicit request =>

    val fileUrl  =
      "https://raw.githubusercontent.com/hmrc/trade-reporting-extracts-frontend/refs/heads/TRE-466/conf/resources/CDS-Report.csv"
    val FileName = "CDS-Report.csv"

    ws.url(fileUrl).stream().map { response =>
      Result(
        header = ResponseHeader(
          OK,
          Map("Content-Disposition" -> s"attachment; filename=$FileName", "Content-Type" -> response.contentType)
        ),
        body = HttpEntity.Streamed(
          data = response.bodyAsSource,
          contentLength = response.headers.get("Content-Length").flatMap(_.headOption).map(_.toLong),
          contentType = Some(response.contentType)
        )
      )
    }
  }
  // download file 500MB
  def download500MBFile(): Action[AnyContent] = Action.async { implicit request =>
    val fileUrl  = "https://link.testfile.org/500MB"
    val FileName = "CDS-Report500MB.csv"

    ws.url(fileUrl).stream().map { response =>
      val contentLength = response.headers.get("Content-Length").flatMap(_.headOption).map(_.toLong)
      val contentType   = response.contentType

      Result(
        header = ResponseHeader(
          OK,
          Map("Content-Disposition" -> s"attachment; filename=$FileName", "Content-Type" -> contentType)
        ),
        body = HttpEntity.Streamed(
          data = response.bodyAsSource,
          contentLength = contentLength,
          contentType = Some(contentType)
        )
      )
    }
  }
  // download file 1GB
    def download1GBFile(): Action[AnyContent] = Action.async { implicit request =>
        val fileUrl  = "https://testfile.org/1.3GBiconpng"
        val FileName = "CDS-Report1GB.csv"

        ws.url(fileUrl).stream().map { response =>
        val contentLength = response.headers.get("Content-Length").flatMap(_.headOption).map(_.toLong)
        val contentType   = response.contentType

        Result(
            header = ResponseHeader(
            OK,
            Map("Content-Disposition" -> s"attachment; filename=$FileName", "Content-Type" -> contentType)
            ),
            body = HttpEntity.Streamed(
            data = response.bodyAsSource,
            contentLength = contentLength,
            contentType = Some(contentType)
            )
        )
        }
    }
}

case class FileRequest(fileUrl: String)
object FileRequest {
  implicit val reads: Reads[FileRequest] = Json.reads[FileRequest]
}
