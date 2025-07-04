package models.audit

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, Json}

import java.time.Instant

class UserLoginEventSpec extends AnyFreeSpec with Matchers {

  "UserLoginEvent" - {
    "must have a valid json output" in {
      val event        = UserLoginEvent("eori", "userId", "affinityGroup", "credentialRole", isSuccessful = true)
      val expectedJson = Json.obj(
        "eori"           -> "eori",
        "userId"         -> "userId",
        "affinityGroup"  -> "affinityGroup",
        "credentialRole" -> "credentialRole",
        "outcome"        -> Json.obj(
          "isSuccessful" -> true,
          "processedAt"  -> Instant.now
        )
      )
      val actualJson   = Json.toJson(event)
      actualJson.as[JsObject] mustEqual expectedJson
    }
  }

}
