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

package services

import connectors.TradeReportingExtractsConnector
import models.{EoriHistory, EoriHistoryResponse}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class EoriHistoryServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  private given HeaderCarrier       = HeaderCarrier()

  "EoriHistoryService" - {
    val mockConnector = mock[TradeReportingExtractsConnector]
    val service       = new EoriHistoryService(mockConnector)(using ec)

    val eori                        = "GB123456789012"
    val history1                    = EoriHistory(eori, Some(LocalDate.parse("2024-01-01")), Some(LocalDate.parse("2024-06-30")))
    val history2                    = EoriHistory(eori, Some(LocalDate.parse("2024-07-01")), Some(LocalDate.parse("2024-12-31")))
    val histories: Seq[EoriHistory] = Seq(history1, history2)

    "fetchEoriHistory should return histories from connector" in {
      when(mockConnector.getEoriHistory(eori)).thenReturn(Future.successful(Some(EoriHistoryResponse(histories))))
      val result: Option[EoriHistoryResponse] = service.fetchEoriHistory(eori).futureValue
      result mustBe Some(EoriHistoryResponse(histories))
    }

    "getFilteredEoriHistory should filter histories by date range" in {
      when(mockConnector.getEoriHistory(eori)).thenReturn(Future.successful(Some(EoriHistoryResponse(histories))))
      val from   = LocalDate.parse("2024-06-01")
      val until  = LocalDate.parse("2024-12-31")
      val result = service.getFilteredEoriHistory(eori, from, until).futureValue
      result mustBe Seq(history1, history2)
    }

    "getFilteredEoriHistory should exclude histories outside date range" in {
      when(mockConnector.getEoriHistory(eori))
        .thenReturn(Future.successful(Some(EoriHistoryResponse(histories))))
      val from   = LocalDate.parse("2024-08-01")
      val until  = LocalDate.parse("2024-12-31")
      val result = service.getFilteredEoriHistory(eori, from, until).futureValue
      result mustBe Seq(history2)
    }

    "getFilteredEoriHistory should return empty if none match" in {
      when(mockConnector.getEoriHistory(eori))
        .thenReturn(Future.successful(Some(EoriHistoryResponse(histories))))
      val from   = LocalDate.parse("2025-01-01")
      val until  = LocalDate.parse("2025-12-31")
      val result = service.getFilteredEoriHistory(eori, from, until).futureValue
      result mustBe Seq.empty
    }
  }
}
