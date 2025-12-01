package utils

import base.SpecBase
import pages.editThirdParty.EditThirdPartyReferencePage

class UserAnswerHelperSpec extends SpecBase {

  "must remove edit third party answers for a given eori" in {
    val helper      = new UserAnswerHelper()
    val userAnswers = emptyUserAnswers.set(EditThirdPartyReferencePage("eori1"), "someRef").get

    val cleanedAnswers = helper.removeEditThirdPartyAnswersForEori("eori1", userAnswers)
    cleanedAnswers.get(EditThirdPartyReferencePage("eori1")) mustBe None
  }

  "must not remove other third party answers when removing for a given eori" in {
    val helper      = new UserAnswerHelper()
    val userAnswers = emptyUserAnswers
      .set(EditThirdPartyReferencePage("eori1"), "someRef")
      .get
      .set(EditThirdPartyReferencePage("eori2"), "anotherRef")
      .get

    val cleanedAnswers = helper.removeEditThirdPartyAnswersForEori("eori1", userAnswers)
    cleanedAnswers.get(EditThirdPartyReferencePage("eori1")) mustBe None
    cleanedAnswers.get(EditThirdPartyReferencePage("eori2")) mustBe Some("anotherRef")
  }

}
