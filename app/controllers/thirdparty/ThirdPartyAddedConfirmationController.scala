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

import config.FrontendAppConfig
import controllers.BaseController
import controllers.actions.*
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ThirdPartyService, TradeReportingExtractsService}
import utils.DateTimeFormats.dateTimeFormat
import views.html.thirdparty.ThirdPartyAddedConfirmationView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ThirdPartyAddedConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  thirdPartyService : ThirdPartyService,
  requireData: DataRequiredAction,
  frontendAppConfig: FrontendAppConfig,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyAddedConfirmationView,
  clock: Clock
) (implicit ec: ExecutionContext)
  extends BaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      thirdPartyAddedConfirmation <- tradeReportingExtractsService.createThirdPartyAddRequest(
        thirdPartyService.buildThirdPartyAddRequest(request.userAnswers, request.eori)
      )
    } yield Ok(view(thirdPartyAddedConfirmation.thirdPartyEori, getDate, frontendAppConfig.exitSurveyUrl))
  }

  private def getDate(implicit messages: Messages): String =
    LocalDate.now(clock).format(dateTimeFormat()(messages.lang))
}
