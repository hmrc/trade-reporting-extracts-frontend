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

package controllers.thirdparty

import controllers.actions.*
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.{dateTimeFormat, formattedSystemTime}
import views.html.thirdparty.RemoveThirdPartyConfirmationView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RemoveThirdPartyConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  clock: Clock,
  view: RemoveThirdPartyConfirmationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val (date, time) = getDateAndTime
      for {
        notificationEmail <- tradeReportingExtractsService.getNotificationEmail(request.eori)
        _                 <- tradeReportingExtractsService.removeThirdParty(request.eori, thirdPartyEori)

      } yield Ok(view(date, time, thirdPartyEori, notificationEmail.address))
  }

  private def getDateAndTime(implicit messages: Messages): (String, String) =
    (
      LocalDate.now(clock).format(dateTimeFormat()(messages.lang)),
      formattedSystemTime(clock)(messages.lang)
    )
}
