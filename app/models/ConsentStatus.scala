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

package models

import play.api.libs.json.*

sealed trait ConsentStatus

object ConsentStatus extends Enumerable.Implicits {

  case object Granted extends WithName("1") with ConsentStatus

  case object Denied extends WithName("0") with ConsentStatus

  val values: Seq[ConsentStatus] = Seq(Granted, Denied)

  implicit val enumerable: Enumerable[ConsentStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def fromString(value: String): ConsentStatus = value match {
    case Granted.toString | "1" => Granted
    case _                      => Denied
  }

  def toString(status: ConsentStatus): String = status match {
    case Granted => "1"
    case Denied  => "0"
  }

  implicit val reads: Reads[ConsentStatus] = Reads {
    case JsString(value) => JsSuccess(fromString(value))
    case JsNull          => JsSuccess(Denied)
    case _               => JsError("ConsentStatus must be a string or null")
  }

  implicit val writes: Writes[ConsentStatus] = Writes { status =>
    JsString(toString(status))
  }

  implicit val format: Format[ConsentStatus] = Format(reads, writes)
}
