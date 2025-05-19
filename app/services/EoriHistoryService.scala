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
import models.EoriHistory
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class EoriHistoryService @Inject() (
                                     tradeReportingExtractsConnector: TradeReportingExtractsConnector
                                   )(using ec: ExecutionContext) {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  def fetchEoriHistory(eori: String): Future[Seq[EoriHistory]] =
    tradeReportingExtractsConnector.getEoriHistory(eori).map(_.getOrElse(Seq.empty))

  def getFilteredEoriHistory(eori: String, from: LocalDate, until: LocalDate): Future[Seq[EoriHistory]] =
    fetchEoriHistory(eori)
      .map(histories => filterHistoriesByDate(histories, from, until))

  private def filterHistoriesByDate(
                                     histories: Seq[EoriHistory],
                                     from: LocalDate,
                                     until: LocalDate
                                   ): Seq[EoriHistory] = {
    histories.filter { h =>
      val validFrom = h.validFrom.getOrElse(LocalDate.MIN)
      val validUntil = h.validUntil.getOrElse(LocalDate.MAX)
      !validUntil.isBefore(from) && !validFrom.isAfter(until)
    }
  }
}