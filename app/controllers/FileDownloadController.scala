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

import java.io.{ByteArrayOutputStream, OutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.Inject
import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

class FileDownloadController @Inject() (
  ws: WSClient,
  override val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController {

  def downloadFile(): Action[AnyContent] = Action.async { implicit request =>
    
    val fileUrl = "https://github.com/hmrc/trade-reporting-extracts-frontend/raw/main/README.md"
    val FileName = "README.md"

    ws.url(fileUrl).stream().map { response =>
      Result(
        header = ResponseHeader(OK, Map(
          "Content-Disposition" -> s"attachment; filename=$FileName",
          "Content-Type" -> response.contentType)),
        body = HttpEntity.Streamed(
          data = response.bodyAsSource,
          contentLength = response.headers.get("Content-Length").flatMap(_.headOption).map(_.toLong),
          contentType = Some(response.contentType)
        )
      )
    }
  }
}