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

package connectors

import play.api.Logging
import play.api.libs.json.Json

import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class TradeReportingExtractsConnector @Inject() (implicit ec: ExecutionContext) extends Logging {

  private val defaultPath                                                = "conf/resources/eoriList.json"
  // TODO replace with a get request to the backend upon implementation of EORI list
  def getEoriList(pathString: String = defaultPath): Future[Seq[String]] = {
    val path = Paths.get(pathString)

    Try {
      val jsonString = new String(Files.readAllBytes(path), "UTF-8")
      Json.parse(jsonString).as[Seq[String]]
    } match {
      case Success(eoriStrings) =>
        Future.successful(eoriStrings)

      case Failure(ex) =>
        val errMsg = s"Failed to read or parse EORI list from file: ${ex.getMessage}"
        logger.error(errMsg)
        Future.failed(new RuntimeException(errMsg, ex))
    }
  }
}
