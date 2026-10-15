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

package forms.behaviours

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.scalacheck.Gen
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Legend
import viewmodels.govuk.date.DateViewModel
import viewmodels.govuk.errorsummary.ErrorSummaryViewModel

class DateBehaviours extends FieldBehaviours {

  def dateField(form: Form[_], key: String, validData: Gen[LocalDate]): Unit =
    "bind valid data" in {

      forAll(validData -> "valid date") { (date: LocalDate) =>
        val data = Map(
          s"$key.day"   -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

  def dateFieldWithMax(form: Form[_], key: String, max: LocalDate, formError: FormError): Unit =
    s"fail to bind a date greater than ${max.format(DateTimeFormatter.ISO_LOCAL_DATE)}" in {

      val generator = datesBetween(max.plusDays(1), max.plusYears(10))

      forAll(generator -> "invalid dates") { (date: LocalDate) =>
        val data = Map(
          s"$key.day"   -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }
    }

  def dateFieldWithMin(form: Form[_], key: String, min: LocalDate, formError: FormError): Unit =
    s"fail to bind a date earlier than ${min.format(DateTimeFormatter.ISO_LOCAL_DATE)}" in {

      val generator = datesBetween(min.minusYears(10), min.minusDays(1))

      forAll(generator -> "invalid dates") { (date: LocalDate) =>
        val data = Map(
          s"$key.day"   -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }
    }

  def mandatoryDateField(form: Form[_], key: String, requiredAllKey: String, errorArgs: Seq[String] = Seq.empty): Unit =
    "fail to bind an empty date" in {

      val result = form.bind(Map.empty[String, String])

      result.errors must contain only FormError(key, requiredAllKey, errorArgs)
    }

  def optionalDateField(form: Form[_], key: String): Unit =
    "bind an empty date" in {

      val result = form.bind(Map.empty[String, String])

      result(key).errors mustBe empty
    }

  def invalidDateField(form: Form[_], invalidKey: String)(implicit messages: Messages): Unit = {
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
        result.errors.map(_.message).distinct mustEqual Seq(invalidKey)

        val dateInput = DateViewModel(result("value"), Legend())
        dateInput.items.map(_.classes.contains("govuk-input--error")) mustEqual expectedErrorClasses

        val errorLinkTarget = expectedErrorKeys.head
        val summary         = ErrorSummaryViewModel(
          result,
          errorLinkOverrides = Map(
            "value"       -> errorLinkTarget,
            "value.day"   -> errorLinkTarget,
            "value.month" -> errorLinkTarget,
            "value.year"  -> errorLinkTarget
          ),
          collapseDuplicateErrors = true
        )
        summary.errorList.size mustEqual 1
        summary.errorList.head.href mustBe Some(s"#$errorLinkTarget")
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

      result.errors must contain only FormError("value.day", invalidKey, Seq.empty)

      val dateInput = DateViewModel(result("value"), Legend())
      dateInput.items.map(_.classes.contains("govuk-input--error")) mustEqual Seq(true, false, false)
    }
  }
}
