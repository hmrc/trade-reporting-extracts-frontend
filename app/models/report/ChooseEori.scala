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

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ChooseEori

object ChooseEori extends Enumerable.Implicits {

  case object Myeori extends WithName("myEori") with ChooseEori
  case object Myauthority extends WithName("myAuthority") with ChooseEori

  val values: Seq[ChooseEori] = Seq(
    Myeori,
    Myauthority
  )

  def options(eori: String)(implicit request: Request[_], messages: Messages): Seq[RadioItem] =
    values.zipWithIndex.map { case (value, index) =>
      RadioItem(
        content = Text(messages(s"chooseEori.${value.toString}", eori)),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
    }

  implicit val enumerable: Enumerable[ChooseEori] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
