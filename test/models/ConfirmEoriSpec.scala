package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ConfirmEoriSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ConfirmEori" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ConfirmEori.values.toSeq)

      forAll(gen) {
        confirmEori =>

          JsString(confirmEori.toString).validate[ConfirmEori].asOpt.value mustEqual confirmEori
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ConfirmEori.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ConfirmEori] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ConfirmEori.values.toSeq)

      forAll(gen) {
        confirmEori =>

          Json.toJson(confirmEori) mustEqual JsString(confirmEori.toString)
      }
    }
  }
}
