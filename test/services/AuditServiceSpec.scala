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

import base.SpecBase
import models.audit.ReportRequestDownloadedAudit
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockAuditConnector = mock[AuditConnector]
  private val service = new AuditService(mockAuditConnector)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
  }

  "AuditService" - {
    "audit" - {
      "must call the audit connector with the correct details" in {
        val auditEvent = ReportRequestDownloadedAudit(
          requestId = "some-id",
          totalReportParts = "1",
          fileUrl = "http://localhost/file",
          fileName = "file.csv",
          fileSizeBytes = "1024",
          reportSubjectEori = "GB123456789000",
          reportTypeName = "TEST_REPORT",
          requesterEori = "GB987654321000"
        )

        service.audit(auditEvent)

        verify(mockAuditConnector, times(1)).sendExplicitAudit(
          eqTo(auditEvent.auditType),
          eqTo(auditEvent)
        )(any(), any(), any())
      }
    }
  }
}