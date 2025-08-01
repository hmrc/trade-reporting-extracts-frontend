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

package forms

import forms.mappings.Mappings
import models.availableReports.AvailableReportDownload
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}

import javax.inject.Inject

class AvailableReportDownloadFormProvider @Inject() extends Mappings {

  def apply(): Form[AvailableReportDownload] =
    Form(
      mapping(
        "reportName"        -> nonEmptyText,
        "referenceNumber"   -> nonEmptyText,
        "reportType"        -> nonEmptyText,
        "reportFilesParts"  -> nonEmptyText,
        "requesterEORI"     -> nonEmptyText,
        "reportSubjectEori" -> nonEmptyText,
        "fileName"          -> nonEmptyText,
        "fileURL"           -> nonEmptyText,
        "fileSize"          -> longNumber
      )(AvailableReportDownload.apply)(a =>
        Some(
          (
            a.reportName,
            a.referenceNumber,
            a.reportType,
            a.reportFilesParts,
            a.requesterEORI,
            a.reportSubjectEori,
            a.fileName,
            a.fileURL,
            a.fileSize
          )
        )
      )
    )
}
