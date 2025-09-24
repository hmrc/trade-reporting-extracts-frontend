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

package generators

import models.*
import models.report.{ChooseEori, Decision, EmailSelection, ReportDateRange, ReportTypeImport}
import models.thirdparty.{ConfirmEori, DataTypes, DeclarationDate}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitrarySelectThirdPartyEori: Arbitrary[SelectThirdPartyEori] =
    Arbitrary {
      Gen.oneOf(SelectThirdPartyEori.values.toSeq)
    }

  implicit lazy val arbitraryConfirmEori: Arbitrary[ConfirmEori] =
    Arbitrary {
      Gen.oneOf(ConfirmEori.values)
    }

  implicit lazy val arbitraryDeclarationDate: Arbitrary[DeclarationDate] =
    Arbitrary {
      Gen.oneOf(DeclarationDate.values.toSeq)
    }

  implicit lazy val arbitraryDataTypes: Arbitrary[DataTypes] =
    Arbitrary {
      Gen.oneOf(DataTypes.values)
    }

  implicit lazy val arbitraryEmailSelection: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("test1@example.com", "test2@example.com", "test3@example.com", EmailSelection.AddNewEmailValue)
    }

  implicit lazy val arbitraryReportDateRange: Arbitrary[ReportDateRange] =
    Arbitrary {
      Gen.oneOf(ReportDateRange.values)
    }

  implicit lazy val arbitraryReportTypeImport: Arbitrary[ReportTypeImport] =
    Arbitrary {
      Gen.oneOf(ReportTypeImport.values)
    }

  implicit lazy val arbitraryChooseEori: Arbitrary[ChooseEori] =
    Arbitrary {
      Gen.oneOf(ChooseEori.values)
    }

  implicit val arbitraryDecision: Arbitrary[Decision] =
    Arbitrary {
      Gen.oneOf(Decision.values)
    }

  implicit lazy val arbitraryEoriRole: Arbitrary[EoriRole] =
    Arbitrary {
      Gen.oneOf(EoriRole.values)
    }

}
