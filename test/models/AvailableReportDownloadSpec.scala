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

package models.availableReports
    
import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

class AvailableReportDownloadSpec extends SpecBase {

  "AvailableReportDownload" - {

    "must serialize and deserialize correctly" in {
      val report = AvailableReportDownload(
        reportName = "Test Report",
        referenceNumber = "REF123",
        reportType = "Test Type",
        reportFilesParts = "1",
        requesterEORI = "EORI123",
        reportSubjectEori = "EORI456",
        fileName = "test.csv",
        fileURL = "http://test.com/file",
        fileSize = 1024L
      )

      val json = Json.obj(
        "reportName" -> "Test Report",
        "referenceNumber" -> "REF123",
        "reportType" -> "Test Type",
        "reportFilesParts" -> "1",
        "requesterEORI" -> "EORI123",
        "reportSubjectEori" -> "EORI456",
        "fileName" -> "test.csv",
        "fileURL" -> "http://test.com/file",
        "fileSize" -> 1024
      )

      Json.toJson(report) mustBe json
      json.validate[AvailableReportDownload] mustBe JsSuccess(report)
    }
  }
}