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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

enum FileType:
  case CSV, XML

object FileType:
  given Format[FileType] with
    def writes(fileType: FileType): JsValue = JsString(fileType.toString)

    def reads(json: JsValue): JsResult[FileType] = json match
      case JsString(value) =>
        FileType.values.find(_.toString == value) match
          case Some(fileType) => JsSuccess(fileType)
          case None           => JsError(s"Unknown FileType: $value")
      case _               => JsError("FileType must be a string")

enum ReportTypeName:
  case IMPORTS_ITEM_REPORT, IMPORTS_HEADER_REPORT, IMPORTS_TAXLINE_REPORT, EXPORTS_ITEM_REPORT

object ReportTypeName:
  given Format[ReportTypeName] with
    def writes(reportTypeName: ReportTypeName): JsValue = JsString(reportTypeName.toString)

    def reads(json: JsValue): JsResult[ReportTypeName] = json match
      case JsString(value) =>
        values.find(_.toString == value) match
          case Some(reportTypeName) => JsSuccess(reportTypeName)
          case None                 => JsError(s"Unknown ReportTypeName: $value")
      case _               => JsError("ReportTypeName must be a string")
