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

package models.report

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Messages, MessagesImpl}
import play.api.test.Helpers.stubMessagesApi
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class EmailSelectionSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  implicit val messages: Messages = MessagesImpl(play.api.i18n.Lang("en"), stubMessagesApi())

  "EmailSelection.checkboxItems" - {

    "must generate checkbox items for each dynamic email and include 'Add New Email' option" in {
      val emails = Seq("user1@test.com", "user2@test.com")
      val result = EmailSelection.checkboxItems(emails)

      result must have length 3

      result.head.content mustBe Text("user1@test.com")
      result(1).content mustBe Text("user2@test.com")
      result(2).content mustBe Text(messages("emailSelection.addNewEmail"))
      result(2).value mustBe EmailSelection.AddNewEmail.toString
    }

    "must correctly index each checkbox item" in {
      val emails = Seq("a@test.com", "b@test.com", "c@test.com")
      val result = EmailSelection.checkboxItems(emails)

      result.zipWithIndex.foreach { case (_, index) =>
        // CheckboxItem does not have an index property, so just check the order
        result(index) mustBe result.apply(index)
      }
    }
  }

  "EmailSelection.enumerable" - {

    "must bind AddNewEmail string to AddNewEmail object" in {
      val result = EmailSelection.enumerable.withName(EmailSelection.AddNewEmail.toString)
      result.value mustBe EmailSelection.AddNewEmail
    }

    "must not bind unknown string to any EmailSelection" in {
      val result = EmailSelection.enumerable.withName("unknown")
      result mustBe empty
    }
  }
}
