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

package forms.thirdparty

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Legend
import viewmodels.govuk.date.DateViewModel
import viewmodels.govuk.errorsummary.ErrorSummaryViewModel

import java.time.LocalDate

class DataStartDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form                        = new DataStartDateFormProvider()()

  val min: LocalDate = LocalDate.now.minusYears(4)
  val max: LocalDate = LocalDate.now.plusYears(5).minusDays(1)

  ".value" - {

    val validData = datesBetween(
      min = min,
      max = max
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "dataStartDate.error.required.all")

    "highlight every invalid date component" in {
      val cases = Seq(
        (
          Map("value.day" -> "a", "value.month" -> "a", "value.year" -> "a"),
          Seq("value.day", "value.month", "value.year"),
          Seq(true, true, true)
        ),
        (
          Map("value.day" -> "1", "value.month" -> "a", "value.year" -> "a"),
          Seq("value.month", "value.year"),
          Seq(false, true, true)
        ),
        (
          Map("value.day" -> "a", "value.month" -> "2", "value.year" -> "a"),
          Seq("value.day", "value.year"),
          Seq(true, false, true)
        ),
        (
          Map("value.day" -> "a", "value.month" -> "a", "value.year" -> "2025"),
          Seq("value.day", "value.month"),
          Seq(true, true, false)
        ),
        (
          Map("value.day" -> "a", "value.month" -> "2", "value.year" -> "2025"),
          Seq("value.day"),
          Seq(true, false, false)
        ),
        (
          Map("value.day" -> "1", "value.month" -> "a", "value.year" -> "2025"),
          Seq("value.month"),
          Seq(false, true, false)
        ),
        (
          Map("value.day" -> "1", "value.month" -> "13", "value.year" -> "2025"),
          Seq("value.month"),
          Seq(false, true, false)
        ),
        (
          Map("value.day" -> "1", "value.month" -> "2", "value.year" -> "a"),
          Seq("value.year"),
          Seq(false, false, true)
        )
      )

      cases.foreach { case (data, expectedErrorKeys, expectedErrorClasses) =>
        val result = form.bind(data)

        result.errors.map(_.key).toSet mustEqual expectedErrorKeys.toSet
        result.errors.map(_.message).distinct mustEqual Seq("dataStartDate.error.invalid")

        val dateInput = DateViewModel(result("value"), Legend())
        dateInput.items.map(_.classes.contains("govuk-input--error")) mustEqual expectedErrorClasses

        val summary = ErrorSummaryViewModel(
          result,
          errorLinkOverrides = Map(
            "value.day"   -> "value.day",
            "value.month" -> "value.day",
            "value.year"  -> "value.day"
          ),
          collapseDuplicateErrors = true
        )
        summary.errorList.size mustEqual 1
        summary.errorList.head.href mustBe Some("#value.day")
      }
    }

    "highlight only the day input when the date is impossible" in {
      val result = form.bind(
        Map(
          "value.day"   -> "31",
          "value.month" -> "2",
          "value.year"  -> "2025"
        )
      )

      result.errors must contain only FormError("value.day", "dataStartDate.error.invalid", Seq.empty)

      val dateInput = DateViewModel(result("value"), Legend())
      dateInput.items.map(_.classes.contains("govuk-input--error")) mustEqual Seq(true, false, false)
    }

    behave like dateFieldWithMin(
      form,
      "value",
      min,
      FormError("value", "dataStartDate.error.min", Seq())
    )
  }
}
