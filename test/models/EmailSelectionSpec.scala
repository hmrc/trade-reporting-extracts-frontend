package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class EmailSelectionSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "EmailSelection" - {

    "must deserialise valid values" in {

      val gen = arbitrary[EmailSelection]

      forAll(gen) {
        emailSelection =>

          JsString(emailSelection.toString).validate[EmailSelection].asOpt.value mustEqual emailSelection
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!EmailSelection.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[EmailSelection] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[EmailSelection]

      forAll(gen) {
        emailSelection =>

          Json.toJson(emailSelection) mustEqual JsString(emailSelection.toString)
      }
    }
  }
}
