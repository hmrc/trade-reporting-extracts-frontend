package pages.thirdparty

import base.SpecBase
import models.thirdparty.DeclarationDate
import utils.json.OptionalLocalDateReads._

import java.time.LocalDate

class DeclarationDatePageSpec extends SpecBase {

  "cleanup" - {

    "must cleanup correctly when all data selection" in {

      val userAnswers = emptyUserAnswers
        .set(
          DataStartDatePage,
          LocalDate.of(2023, 1, 1)
        )
        .success
        .value
        .set(
          DataEndDatePage,
          Some(LocalDate.of(2023, 12, 31))
        )
        .success
        .value

      val cleanedUserAnswers =
        DeclarationDatePage.cleanup(Some(DeclarationDate.AllAvailableData), userAnswers).success.value

      cleanedUserAnswers.get(DataStartDatePage) mustBe None
      cleanedUserAnswers.get(DataEndDatePage) mustBe None
    }

    "must clean up correctly when custom date range selected" in {

      val userAnswers = emptyUserAnswers
        .set(
          DataStartDatePage,
          LocalDate.of(2023, 1, 1)
        )
        .success
        .value
        .set(
          DataEndDatePage,
          Some(LocalDate.of(2023, 12, 31))
        )
        .success
        .value

      val cleanedUserAnswers =
        DeclarationDatePage.cleanup(Some(DeclarationDate.CustomDateRange), userAnswers).success.value

      cleanedUserAnswers.get(DataStartDatePage) mustBe Some(LocalDate.of(2023, 1, 1))
      cleanedUserAnswers.get(DataEndDatePage).get mustBe Some(LocalDate.of(2023, 12, 31))
    }
  }
}
