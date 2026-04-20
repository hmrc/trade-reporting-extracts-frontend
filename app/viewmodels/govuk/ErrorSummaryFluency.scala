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

package viewmodels.govuk

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.{ErrorLink, ErrorSummary}

object errorsummary extends ErrorSummaryFluency

trait ErrorSummaryFluency {

  object ErrorSummaryViewModel {

    def apply(
      form: Form[_],
      errorLinkOverrides: Map[String, String] = Map.empty
    )(implicit messages: Messages): ErrorSummary = {

      val errors = form.errors.map { error =>

        val errorLink = error.args match {
          case a if a.contains("day")      => s"${error.key}.day"
          case a if a.contains("month")    => s"${error.key}.month"
          case a if a.contains("year")     => s"${error.key}.year"
          case a if a.contains("diwrnod")  => s"${error.key}.day"
          case a if a.contains("mis")      => s"${error.key}.month"
          case a if a.contains("blwyddyn") => s"${error.key}.year"
          case _                           => error.key
        }

        ErrorLink(
          href = Some(s"#${errorLinkOverrides.getOrElse(errorLink, errorLink)}"),
          content = Text(messages(error.message, error.args: _*))
        )
      }

      ErrorSummary(
        errorList = errors,
        title = Text(messages("error.summary.title"))
      )
    }
  }

  implicit class FluentErrorSummary(errorSummary: ErrorSummary) {

    def withDescription(description: Content): ErrorSummary =
      errorSummary.copy(description = description)

    def withCssClass(newClass: String): ErrorSummary =
      errorSummary.copy(classes = s"${errorSummary.classes} $newClass")

    def withAttribute(attribute: (String, String)): ErrorSummary =
      errorSummary.copy(attributes = errorSummary.attributes + attribute)
  }
}
