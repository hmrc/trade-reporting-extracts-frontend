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

package models.report

import models.ReportTypeName
import models.availableReports.{AvailableReportAction, AvailableReportsViewModel, AvailableThirdPartyReportsViewModel, AvailableUserReportsViewModel}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsValue, Json}

import java.time.Instant

class AvailableReportsViewModelSpec extends AnyFreeSpec with Matchers {

  val userReportsData: Seq[AvailableUserReportsViewModel]             = Seq(
    AvailableUserReportsViewModel(
      "userReport1",
      "userReportRef1",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      Seq(AvailableReportAction("action", "url", 0L, models.FileType.CSV))
    ),
    AvailableUserReportsViewModel(
      "userReport2",
      "userReportRef2",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      Seq(AvailableReportAction("action", "url", 0L, models.FileType.CSV))
    )
  )
  val thirdPartyReportsData: Seq[AvailableThirdPartyReportsViewModel] = Seq(
    AvailableThirdPartyReportsViewModel(
      "TPReportName1",
      "reportRef1",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      "1-1-2000",
      Seq(AvailableReportAction("action", "url", 0L, models.FileType.CSV))
    ),
    AvailableThirdPartyReportsViewModel(
      "TPReportName2",
      "reportRef2",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      "1-1-2000",
      Seq(AvailableReportAction("action", "url", 0L, models.FileType.CSV))
    )
  )

  "AvailableReportsViewModel" - {

    "should parse full JSON correctly" in {
      val json = Json.parse("""{
            "availableUserReports": [{
              "reportName": "userReport1",
              "referenceNumber": "userReportRef1",
              "expiryDate": "2000-01-01T00:00:00Z",
              "reportType": "IMPORTS_ITEM_REPORT",
              "reportFilesParts" : "1",
              "requesterEORI" : "GB000123456789",
              "reportSubjectEori" : "GB000123456789",
              "action": [
                {
                  "fileName": "action",
                  "fileURL": "url",
                  "size": 0,
                  "fileType": "CSV"
                }
              ]
            },
            {
              "reportName": "userReport2",
              "referenceNumber": "userReportRef2",
              "expiryDate": "2000-01-01T00:00:00Z",
              "reportType": "IMPORTS_ITEM_REPORT",
               "reportFilesParts" : "1",
              "requesterEORI" : "GB000123456789",
              "reportSubjectEori" : "GB000123456789",
              "action": [
                {
                  "fileName": "action",
                  "fileURL": "url",
                  "size": 0,
                  "fileType": "CSV"
                }
              ]
            }],
            "availableThirdPartyReports":[ {
              "reportName": "TPReportName1",
              "referenceNumber": "reportRef1",
              "expiryDate": "2000-01-01T00:00:00Z",
              "reportType": "IMPORTS_ITEM_REPORT",
              "companyName": "1-1-2000",
              "action": [
                {
                  "fileName": "action",
                  "fileURL": "url",
                  "size": 0,
                  "fileType": "CSV"
                }
              ]
            },
            {
              "reportName": "TPReportName2",
              "referenceNumber": "reportRef2",
              "expiryDate": "2000-01-01T00:00:00Z",
              "reportType": "IMPORTS_ITEM_REPORT",
              "companyName": "1-1-2000",
              "action": [
                {
                  "fileName": "action",
                  "fileURL": "url",
                  "size": 0,
                  "fileType": "CSV"
                }
              ]
            }]
         }""".stripMargin)

      val expected = AvailableReportsViewModel(Some(userReportsData), Some(thirdPartyReportsData))
      json.validate[AvailableReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse JSON with only userReports" in {
      val json = Json.parse("""{
            "availableUserReports": [
                    {
                      "reportName": "userReport1",
                      "referenceNumber": "userReportRef1",
                      "expiryDate": "2000-01-01T00:00:00Z",
                      "reportType": "IMPORTS_ITEM_REPORT",
                       "reportFilesParts" : "1",
                      "requesterEORI" : "GB000123456789",
                      "reportSubjectEori" : "GB000123456789",
                      "action": [
                        {
                          "fileName": "action",
                          "fileURL": "url",
                          "size": 0,
                          "fileType": "CSV"
                        }
                      ]
                    }
                  ]
         }""".stripMargin)

      val expected = AvailableReportsViewModel(Some(Seq(userReportsData.head)), None)
      json.validate[AvailableReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse JSON with only thirdPartyReports" in {
      val json = Json.parse("""
          |{
          |  "availableThirdPartyReports":[ {
          |              "reportName": "TPReportName1",
          |              "referenceNumber": "reportRef1",
          |              "expiryDate": "2000-01-01T00:00:00Z",
          |              "reportType": "IMPORTS_ITEM_REPORT",
          |              "companyName": "1-1-2000",
          |              "action": [
          |                {
          |                  "fileName": "action",
          |                  "fileURL": "url",
          |                  "size": 0,
          |                  "fileType": "CSV"
          |                }
          |              ]
          |            }]
          |}
          |""".stripMargin)

      val expected = AvailableReportsViewModel(None, Some(Seq(thirdPartyReportsData.head)))
      json.validate[AvailableReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse empty JSON as None for both fields" in {
      val json     = Json.parse("""{}""")
      val expected = AvailableReportsViewModel(None, None)
      json.validate[AvailableReportsViewModel].asOpt mustBe Some(expected)
    }

    "should serialize to JSON correctly" in {
      val model = AvailableReportsViewModel(Some(userReportsData), Some(thirdPartyReportsData))
      val json  = Json.toJson(model)

      (json \ "availableUserReports")(0).\("reportName").as[String] mustBe "userReport1"
      (json \ "availableThirdPartyReports")(1).\("reportName").as[String] mustBe "TPReportName2"
    }
  }
}
