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

package controllers

import base.SpecBase
import forms.AvailableReportDownloadFormProvider
import models.UserAnswers
import models.availableReports.AvailableReportDownload
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.AuditService

import scala.concurrent.Future

class AvailableReportFileDownloadControllerSpec extends SpecBase with BeforeAndAfterEach with Matchers {

  private val mockWsClient = mock[WSClient]
  private val mockWsRequest = mock[WSRequest]
  private val mockAuditService = mock[AuditService]
  private val mockFormProvider = mock[AvailableReportDownloadFormProvider]
  private val mockForm = mock[Form[AvailableReportDownload]]
  private val mockWsResponse = mock[WSResponse]

  override def applicationBuilder(userAnswers: Option[UserAnswers] = Some(emptyUserAnswers)): GuiceApplicationBuilder =
    super.applicationBuilder(userAnswers)
      .overrides(
        bind[WSClient].toInstance(mockWsClient),
        bind[AuditService].toInstance(mockAuditService),
        bind[AvailableReportDownloadFormProvider].toInstance(mockFormProvider)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockWsClient, mockAuditService, mockFormProvider, mockForm, mockWsRequest, mockWsResponse)
  }

  "AvailableReportFileDownloadController" - {
    ".downloadFile" - {
      "must return OK and stream the file when form submission is valid" in {
        val testData = "test data"
        val testReport = AvailableReportDownload(
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
        when(mockFormProvider.apply()).thenReturn(mockForm)
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(mockForm)
        when(mockForm.fold(any(), any())).thenAnswer { invocation =>
          val successFunc = invocation.getArgument[AvailableReportDownload => Future[Any]](1)
          successFunc(testReport)
        }
        when(mockWsClient.url(any[String])).thenReturn(mockWsRequest)
        when(mockWsRequest.stream()).thenReturn(Future.successful(mockWsResponse))
        when(mockWsResponse.status).thenReturn(OK)
        when(mockWsResponse.headers).thenReturn(Map("Content-Length" -> Seq("1024")))
        when(mockWsResponse.contentType).thenReturn("text/csv")
        when(mockWsResponse.bodyAsSource).thenReturn(Source.single(ByteString(testData)))

        val app = applicationBuilder().build()

        val request = FakeRequest(POST, routes.AvailableReportFileDownloadController.availableReportDownloadFile().url)
          .withFormUrlEncodedBody(
            "reportName" -> testReport.reportName,
            "referenceNumber" -> testReport.referenceNumber,
            "reportType" -> testReport.reportType,
            "reportFilesParts" -> testReport.reportFilesParts,
            "requesterEORI" -> testReport.requesterEORI,
            "reportSubjectEori" -> testReport.reportSubjectEori,
            "fileName" -> testReport.fileName,
            "fileURL" -> testReport.fileURL,
            "fileSize" -> testReport.fileSize.toString
          )

        val result = route(app, request).value
        status(result) mustBe OK
        contentType(result) mustBe Some("text/csv")
        header("Content-Disposition", result).value mustBe s"attachment; filename=${testReport.fileName}"
        verify(mockAuditService, times(1)).audit(any())(using any(),any())
      }

      "must return BadRequest when form submission has errors" in {
        when(mockFormProvider.apply()).thenReturn(mockForm)
        when(mockForm.bindFromRequest()(any(), any())).thenReturn(mockForm)
        when(mockForm.fold(any(), any())).thenAnswer(i =>
          i.getArgument[Form[AvailableReportDownload] => Future[Any]](0)(mockForm)
        )
        val app = applicationBuilder().build()
        val request = FakeRequest(POST, routes.AvailableReportFileDownloadController.availableReportDownloadFile().url)
          .withFormUrlEncodedBody("invalid" -> "data")
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error processing request"
      }
    }
  }
}