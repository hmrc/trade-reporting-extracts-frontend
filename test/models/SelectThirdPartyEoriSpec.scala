package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class SelectThirdPartyEoriSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "SelectThirdPartyEori" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(SelectThirdPartyEori.values.toSeq)

      forAll(gen) { selectThirdPartyEori =>
        JsString(selectThirdPartyEori.toString)
          .validate[SelectThirdPartyEori]
          .asOpt
          .value mustEqual selectThirdPartyEori
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!SelectThirdPartyEori.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[SelectThirdPartyEori] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(SelectThirdPartyEori.values.toSeq)

      forAll(gen) { selectThirdPartyEori =>
        Json.toJson(selectThirdPartyEori) mustEqual JsString(selectThirdPartyEori.toString)
      }
    }
  }
}
