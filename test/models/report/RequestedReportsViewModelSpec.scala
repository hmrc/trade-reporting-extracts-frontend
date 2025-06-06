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

import models.ReportStatus.IN_PROGRESS
import models.ReportTypeName
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import utils.ReportHelpers

import java.time.Instant

class RequestedReportsViewModelSpec extends AnyFreeSpec with Matchers {

  val userReportsData: Seq[RequestedUserReportViewModel] = Seq(
    RequestedUserReportViewModel(
      "userReportRef1",
      "userReport1",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      IN_PROGRESS
    ),
    RequestedUserReportViewModel(
      "userReportRef2",
      "userReport2",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      IN_PROGRESS
    )
  )

  val thirdPartyReportsData: Seq[RequestedThirdPartyReportViewModel] = Seq(
    RequestedThirdPartyReportViewModel(
      "reportRef1",
      "TPReportName1",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      "Example Ltd",
      IN_PROGRESS
    ),
    RequestedThirdPartyReportViewModel(
      "reportRef2",
      "TPReportName2",
      Instant.parse("2000-01-01T00:00:00Z"),
      ReportTypeName.IMPORTS_ITEM_REPORT,
      "Example Ltd",
      IN_PROGRESS
    )
  )

  "RequestedReportsViewModel" - {

    "should parse full JSON correctly" in {
      val json = Json.parse("""
          |{
          |  "userReports": [
          |    {
          |      "referenceNumber": "userReportRef1",
          |      "reportName": "userReport1",
          |      "requestedDate": "2000-01-01T00:00:00Z",
          |      "reportType": "IMPORTS_ITEM_REPORT",
          |      "reportStatus": "IN_PROGRESS"
          |    },
          |    {
          |      "referenceNumber": "userReportRef2",
          |      "reportName": "userReport2",
          |      "requestedDate": "2000-01-01T00:00:00Z",
          |      "reportType": "IMPORTS_ITEM_REPORT",
          |      "reportStatus": "IN_PROGRESS"
          |    }
          |  ],
          |  "thirdPartyReports": [
          |    {
          |      "referenceNumber": "reportRef1",
          |      "reportName": "TPReportName1",
          |      "requestedDate": "2000-01-01T00:00:00Z",
          |      "reportType": "IMPORTS_ITEM_REPORT",
          |      "companyName": "Example Ltd",
          |      "reportStatus": "IN_PROGRESS"
          |    },
          |    {
          |      "referenceNumber": "reportRef2",
          |      "reportName": "TPReportName2",
          |      "requestedDate": "2000-01-01T00:00:00Z",
          |      "reportType": "IMPORTS_ITEM_REPORT",
          |      "companyName": "Example Ltd",
          |      "reportStatus": "IN_PROGRESS"
          |    }
          |  ]
          |}
          |""".stripMargin)

      val expected = RequestedReportsViewModel(Some(userReportsData), Some(thirdPartyReportsData))
      json.validate[RequestedReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse JSON with only userReports" in {
      val json = Json.parse("""
          |{
          |  "userReports": [
          |    {
          |      "referenceNumber": "userReportRef1",
          |      "reportName": "userReport1",
          |      "requestedDate": "2000-01-01T00:00:00Z",
          |      "reportType": "IMPORTS_ITEM_REPORT",
          |      "reportStatus": "IN_PROGRESS"
          |    }
          |  ]
          |}
          |""".stripMargin)

      val expected = RequestedReportsViewModel(Some(Seq(userReportsData.head)), None)
      json.validate[RequestedReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse JSON with only thirdPartyReports" in {
      val json = Json.parse("""
          |{
          |  "thirdPartyReports": [
          |    {
          |      "referenceNumber": "reportRef1",
          |      "reportName": "TPReportName1",
          |      "requestedDate": "2000-01-01T00:00:00Z",
          |      "reportType": "IMPORTS_ITEM_REPORT",
          |      "companyName": "Example Ltd",
          |      "reportStatus": "IN_PROGRESS"
          |    }
          |  ]
          |}
          |""".stripMargin)

      val expected = RequestedReportsViewModel(None, Some(Seq(thirdPartyReportsData.head)))
      json.validate[RequestedReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse empty JSON as None for both fields" in {
      val json     = Json.parse("""{}""")
      val expected = RequestedReportsViewModel(None, None)
      json.validate[RequestedReportsViewModel].asOpt mustBe Some(expected)
    }

    "should serialize to JSON correctly" in {
      val model = RequestedReportsViewModel(Some(userReportsData), Some(thirdPartyReportsData))
      val json  = Json.toJson(model)

      (json \ "userReports")(0).\("referenceNumber").as[String] mustBe "userReportRef1"
      (json \ "thirdPartyReports")(1).\("referenceNumber").as[String] mustBe "reportRef2"
    }
  }

  "RequestedUserReportViewModel" - {
    val model = userReportsData.head

    "should serialize and deserialize to/from JSON" in {
      val json = Json.toJson(model)
      json.as[RequestedUserReportViewModel] mustBe model
    }

    "should format requested date correctly" in {
      model.formattedRequestedDate mustBe "1 Jan 2000"
    }

    "should format report type correctly" in {
      model.formattedReportType mustBe ReportHelpers.getReportType(model.reportType)
    }
  }

  "RequestedThirdPartyReportViewModel" - {
    val model = thirdPartyReportsData.head

    "should serialize and deserialize to/from JSON" in {
      val json = Json.toJson(model)
      json.as[RequestedThirdPartyReportViewModel] mustBe model
    }

    "should format requested date correctly" in {
      model.formattedRequestedDate mustBe "1 Jan 2000"
    }

    "should format report type correctly" in {
      model.formattedReportType mustBe ReportHelpers.getReportType(model.reportType)
    }
  }
}
