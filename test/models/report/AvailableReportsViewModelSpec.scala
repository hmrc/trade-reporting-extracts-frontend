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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsValue, Json}

class AvailableReportsViewModelSpec extends AnyFreeSpec with Matchers {

  val userReportsData: Seq[AvailableUserReportsViewModel]             = Seq(
    AvailableUserReportsViewModel("userReport1", "userReportRef1", "reportType1", "1-1-2000", "action"),
    AvailableUserReportsViewModel("userReport2", "userReportRef2", "reportType2", "1-1-2000", "action")
  )
  val thirdPartyReportsData: Seq[AvailableThirdPartyReportsViewModel] = Seq(
    AvailableThirdPartyReportsViewModel(
      "TPReportName1",
      "reportRef1",
      "business1",
      "reportType1",
      "1-1-2000",
      "action"
    ),
    AvailableThirdPartyReportsViewModel("TPReportName2", "reportRef2", "business2", "reportType2", "1-1-2000", "action")
  )

  "AvailableReportsViewModel" - {

    "should parse full JSON correctly" in {
      val json = Json.parse("""
          |{
          |  "userReports": [
          |    { "reportName": "userReport1", "referenceNumber": "userReportRef1", "reportType": "reportType1", "expiryDate": "1-1-2000", "action": "action" },
          |    { "reportName": "userReport2", "referenceNumber": "userReportRef2", "reportType": "reportType2", "expiryDate": "1-1-2000", "action": "action" }
          |  ],
          |  "thirdPartyReports": [
          |    { "reportName": "TPReportName1", "referenceNumber": "reportRef1", "companyName": "business1", "reportType": "reportType1", "expiryDate": "1-1-2000", "action": "action" },
          |    { "reportName": "TPReportName2", "referenceNumber": "reportRef2", "companyName": "business2", "reportType": "reportType2", "expiryDate": "1-1-2000", "action": "action" }
          |  ]
          |}
          |""".stripMargin)

      val expected = AvailableReportsViewModel(Some(userReportsData), Some(thirdPartyReportsData))
      json.validate[AvailableReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse JSON with only userReports" in {
      val json = Json.parse("""
          |{
          |  "userReports": [
          |    { "reportName": "userReport1", "referenceNumber": "userReportRef1", "reportType": "reportType1", "expiryDate": "1-1-2000", "action": "action" }
          |  ]
          |}
          |""".stripMargin)

      val expected = AvailableReportsViewModel(Some(Seq(userReportsData.head)), None)
      json.validate[AvailableReportsViewModel].asOpt mustBe Some(expected)
    }

    "should parse JSON with only thirdPartyReports" in {
      val json = Json.parse("""
          |{
          |  "thirdPartyReports": [
          |    { "reportName": "TPReportName1", "referenceNumber": "reportRef1", "companyName": "business1", "reportType": "reportType1", "expiryDate": "1-1-2000", "action": "action" }
          |  ]
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

      (json \ "userReports")(0).\("reportName").as[String] mustBe "userReport1"
      (json \ "thirdPartyReports")(1).\("reportName").as[String] mustBe "TPReportName2"
    }
  }
}
