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

import base.SpecBase
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import play.api.{Application, inject}

import scala.concurrent.Future

class TradeReportingExtractsConnectorSpec extends SpecBase with ScalaFutures {

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.trade-reporting-extracts.port" -> 1234)
      .build()

  "TradeReportingExtractsConnector" - {

    "getEoriList" - {

      "must return a list of EORI numbers when the file is read successfully" in {
        val app = application

        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          val result    = connector.getEoriList().futureValue

          result mustBe Seq("Eori1", "Eori2", "Eori3")
        }
      }

      "must log an error and return a failed future if the file cannot be read" in {
        val app = application
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]

          val mockConnector = mock[TradeReportingExtractsConnector]
          when(mockConnector.getEoriList())
            .thenReturn(Future.failed(new RuntimeException("Failed to read or parse EORI list from file")))

          val thrown = intercept[RuntimeException] {
            mockConnector.getEoriList().futureValue
          }

          thrown.getMessage must include("Failed to read or parse EORI list from file")
        }
      }

      "must return an empty sequence if the JSON is invalid or empty" in {
        val app = application

        val path = "conf/resources/emptyEoriList.json"
        running(app) {
          val connector = app.injector.instanceOf[TradeReportingExtractsConnector]
          val result    = connector.getEoriList(path).futureValue

          result mustBe Seq.empty
        }
      }

    }
  }
}
