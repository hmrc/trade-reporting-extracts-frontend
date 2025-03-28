package models

import generators.ModelGenerators
import generators.arbitraryEoriRole
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class EoriRoleSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with ModelGenerators {

  "EoriRole" - {

    "must deserialise valid values" in {

      val gen = arbitrary[EoriRole]

      forAll(gen) { eoriRole =>
        JsString(eoriRole.toString).validate[EoriRole].asOpt.value mustEqual eoriRole
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!EoriRole.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[EoriRole] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[EoriRole]

      forAll(gen) { eoriRole =>
        Json.toJson(eoriRole) mustEqual JsString(eoriRole.toString)
      }
    }
  }
}
