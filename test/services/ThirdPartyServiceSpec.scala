package services

import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.TryValues._
import models.thirdparty.{ThirdPartyRequest, DeclarationDate, DataTypes}
import models.UserAnswers
import pages.thirdparty._
import java.time.{Clock, Instant, LocalDate, ZoneId}

class ThirdPartyServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  val fixedInstant: Instant = Instant.parse("2024-01-01T00:00:00Z")
  val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
  val service = new ThirdPartyService(fixedClock)

  "ThirdPartyService" should {

    "buildThirdPartyAddRequest" should {

      "populate all fields correctly when all data is present" in {
        val userAnswers = UserAnswers("id")
          .set(EoriNumberPage, "GB2").success.value
          .set(ThirdPartyAccessStartDatePage, LocalDate.of(2024, 1, 1)).success.value
          .set(DataTypesPage, Set(DataTypes.Import)).success.value
          .set(ThirdPartyReferencePage, "ref").success.value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")

        result.userEORI mustBe "GB1"
        result.thirdPartyEORI mustBe "GB2"
        result.accessType mustBe Set("IMPORT")
        result.accessStart mustBe LocalDate.of(2024, 1, 1).atStartOfDay(fixedClock.getZone).toInstant
        result.accessEnd mustBe None
        result.referenceName mustBe Some("ref")
      }

      "set reportDateStart and reportDateEnd to None for AllAvailableData" in {
        val userAnswers = UserAnswers("id")
          .set(DeclarationDatePage, DeclarationDate.AllAvailableData).success.value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        result.reportDateStart mustBe None
        result.reportDateEnd mustBe None
      }

      "set reportDateStart and reportDateEnd for CustomDateRange" in {
        val customDate = LocalDate.of(2023, 12, 31)
        val userAnswers = UserAnswers("id")
          .set(DeclarationDatePage, DeclarationDate.CustomDateRange).success.value
          .set(DataStartDatePage, customDate).success.value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        val expectedInstant = customDate.atStartOfDay(fixedClock.getZone).toInstant
        result.reportDateStart mustBe Some(expectedInstant)
        result.reportDateEnd mustBe Some(expectedInstant)
      }

      "use current instant for accessStart if not present" in {
        val userAnswers = UserAnswers("id")
        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        result.accessStart mustBe fixedInstant
      }

      "set accessEnd if present" in {
        val endDate = LocalDate.of(2024, 2, 1)
        val userAnswers = UserAnswers("id")
          .set(ThirdPartyAccessEndDatePage, Some(endDate)).success.value

        val result = service.buildThirdPartyAddRequest(userAnswers, "GB1")
        result.accessEnd mustBe Some(endDate.atStartOfDay(fixedClock.getZone).toInstant)
      }
    }
  }
}