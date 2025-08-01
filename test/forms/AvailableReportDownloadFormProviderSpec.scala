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

import base.SpecBase
import models.availableReports.AvailableReportDownload
import play.api.data.{Form, FormError}

class AvailableReportDownloadFormProviderSpec extends SpecBase {

  private val formProvider                        = new AvailableReportDownloadFormProvider()
  private val form: Form[AvailableReportDownload] = formProvider()

  private val validData = Map(
    "reportName"        -> "Test Report",
    "referenceNumber"   -> "REF123",
    "reportType"        -> "Test Type",
    "reportFilesParts"  -> "1",
    "requesterEORI"     -> "EORI123",
    "reportSubjectEori" -> "EORI456",
    "fileName"          -> "test.csv",
    "fileURL"           -> "http://test.com/file",
    "fileSize"          -> "1024"
  )

  private val expectedModel = AvailableReportDownload(
    reportName = "Test Report",
    referenceNumber = "REF123",
    reportType = "Test Type",
    reportFilesParts = "1",
    requesterEORI = "EORI123",
    reportSubjectEori = "EORI456",
    fileName = "test.csv",
    fileURL = "http://test.com/file",
    fileSize = 1024L
  )

  "AvailableReportDownloadFormProvider" - {

    "must bind valid data correctly" in {
      val result = form.bind(validData)
      result.value mustBe Some(expectedModel)
      result.hasErrors mustBe false
    }

    "must fail to bind when a required text field is empty" in {
      val requiredTextFields = Seq(
        "reportName",
        "referenceNumber",
        "reportType",
        "reportFilesParts",
        "requesterEORI",
        "reportSubjectEori",
        "fileName",
        "fileURL"
      )

      for (field <- requiredTextFields) {
        val data   = validData.updated(field, "")
        val result = form.bind(data)
        result.errors must contain(FormError(field, "error.required"))
      }
    }

    "must unbind a model to a map of data" in {
      val filledForm = form.fill(expectedModel)
      filledForm.data mustBe validData
    }
  }
}
