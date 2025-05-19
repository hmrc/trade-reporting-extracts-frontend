package models
import java.time.LocalDate
import models.EoriHistory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class EoriHistorySpec extends AnyFreeSpec with Matchers {

  "EoriHistory" - {

    "must serialize and deserialize correctly with valid dates" in {
      val eoriHistory = EoriHistory("GB123456789000", Some(LocalDate.parse("2024-01-01")), Some(LocalDate.parse("2024-12-31")))
      val json = Json.toJson(eoriHistory)
      json mustBe Json.obj(
        "eori" -> "GB123456789000",
        "validFrom" -> "2024-01-01",
        "validUntil" -> "2024-12-31"
      )
      json.as[EoriHistory] mustBe eoriHistory
    }

    "must handle missing dates" in {
      val eoriHistory = EoriHistory("GB123456789000", None, None)
      val json = Json.toJson(eoriHistory)
      (json \ "validFrom").asOpt[String] mustBe None
      (json \ "validUntil").asOpt[String] mustBe None
      json.as[EoriHistory] mustBe eoriHistory
    }

    "must parse ISO_OFFSET_DATE_TIME for validFrom and validUntil" in {
      val json = Json.obj(
        "eori" -> "GB123456789000",
        "validFrom" -> "2024-01-01T00:00:00+00:00",
        "validUntil" -> "2024-12-31T23:59:59+00:00"
      )
      val result = json.as[EoriHistory]
      result.validFrom mustBe Some(LocalDate.parse("2024-01-01"))
      result.validUntil mustBe Some(LocalDate.parse("2024-12-31"))
    }

    "must log and return None for invalid date formats" in {
      val json = Json.obj(
        "eori" -> "GB123456789000",
        "validFrom" -> "not-a-date"
      )
      val result = json.as[EoriHistory]
      result.validFrom mustBe None
    }
  }
}