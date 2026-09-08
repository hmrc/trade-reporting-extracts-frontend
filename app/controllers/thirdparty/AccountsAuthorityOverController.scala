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
import models.UserActiveStatus
import models.thirdparty.{AccountAuthorityOverViewModel, EoriBusinessAccessInfo}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.AccountsAuthorityOverView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AccountsAuthorityOverController @Inject() (
  override val messagesApi: MessagesApi,
  clock: Clock = Clock.systemUTC(),
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: AccountsAuthorityOverView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    tradeReportingExtractsService.getAccountsAuthorityOver(request.eori).map { accountsAuthorityOver =>
      val viewModel = accountsAuthorityOver.map(translateToViewModel)
      Ok(view(viewModel))
    }
  }

  private def translateToViewModel(authorisedThirdParty: EoriBusinessAccessInfo): AccountAuthorityOverViewModel =
    AccountAuthorityOverViewModel(
      eori = authorisedThirdParty.eori,
      businessInfo = authorisedThirdParty.businessInfo,
      status = Some(
        UserActiveStatus.fromInstants(
          authorisedThirdParty.accessStart,
          authorisedThirdParty.reportDataStart,
          clock
        )
      )
    )
}
