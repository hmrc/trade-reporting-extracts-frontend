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
import models.{CompanyInformation, ConsentStatus, ThirdPartyDetails}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.thirdparty.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.thirdparty.BusinessDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BusinessDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessDetailsView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(traderEori: String): Action[AnyContent] = (identify andThen getOrCreate).async { implicit request =>
    for {
      companyInfo       <- tradeReportingExtractsService.getCompanyInformation(traderEori)
      maybeCompanyName   = resolveDisplayName(companyInfo)
      thirdPartyDetails <- tradeReportingExtractsService.getAuthorisedBusinessDetails(request.eori, traderEori)
      rows               = rowGenerator(thirdPartyDetails, maybeCompanyName, traderEori)
      list               = SummaryListViewModel(rows = rows.flatten)
    } yield Ok(view(list))
  }

  private def rowGenerator(
    thirdPartyDetails: ThirdPartyDetails,
    businessInfo: String,
    traderEori: String
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    Seq(
      EoriNumberSummary.detailsRow(traderEori),
      BusinessInfoSummary.row(businessInfo),
      ThirdPartyAccessPeriodSummary.businessDetailsRow(thirdPartyDetails),
      DataTypesSummary.businessDetailsRow(thirdPartyDetails.dataTypes),
      DataTheyCanViewSummary.businessDetailsRow(thirdPartyDetails)
    )

  private def resolveDisplayName(companyInfo: CompanyInformation)(implicit messages: Messages): String =
    companyInfo.consent match {
      case ConsentStatus.Denied => messages("confirmEori.noConsent")
      case _                    => companyInfo.name
    }
}
