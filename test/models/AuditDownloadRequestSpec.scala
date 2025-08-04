package models

import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

class AuditDownloadRequestSpec extends SpecBase {

  "AuditDownloadRequest" - {

    "must serialize and deserialize correctly" in {
      val auditDownloadRequest = AuditDownloadRequest(
        reportReference = "some-reference",
        fileName = "report.csv",
        fileUrl = "http://localhost/report.csv"
      )

      val json = Json.obj(
        "reportReference" -> "some-reference",
        "fileName" -> "report.csv",
        "fileUrl" -> "http://localhost/report.csv"
      )

      Json.toJson(auditDownloadRequest) mustBe json
      json.validate[AuditDownloadRequest] mustBe JsSuccess(auditDownloadRequest)
    }
  }
}