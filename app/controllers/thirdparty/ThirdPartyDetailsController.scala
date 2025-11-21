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
import models.{CompanyInformation, ConsentStatus, ThirdPartyDetails, UserActiveStatus, UserAnswers}
import pages.editThirdParty.{EditThirdPartyDataTypesPage, EditThirdPartyReferencePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.thirdparty.{BusinessInfoSummary, DataTheyCanViewSummary, DataTypesSummary, EoriNumberSummary, ThirdPartyAccessPeriodSummary, ThirdPartyReferenceSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.thirdparty.ThirdPartyDetailsView
import utils.DateTimeFormats.computeCalculatedDateValue

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ThirdPartyDetailsController @Inject (clock: Clock = Clock.systemUTC())(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getOrCreate: DataRetrievalOrCreateAction,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyDetailsView,
  tradeReportingExtractsService: TradeReportingExtractsService,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends BaseController
    with I18nSupport {

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getOrCreate).async {
    implicit request =>
      for {
        companyInfo        <- tradeReportingExtractsService.getCompanyInformation(thirdPartyEori)
        maybeCompanyName    = resolveDisplayName(companyInfo)
        thirdPartyDetails  <- tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori)
        status              = UserActiveStatus.fromInstants(
                                thirdPartyDetails.accessStartDate.atStartOfDay(clock.getZone()).toInstant,
                                thirdPartyDetails.dataStartDate.map(_.atStartOfDay(clock.getZone()).toInstant),
                                clock
                              )
        calculatedDateValue = computeCalculatedDateValue(thirdPartyDetails, status)
        rows                = rowGenerator(thirdPartyDetails, maybeCompanyName, thirdPartyEori, request.userAnswers)
        list                = SummaryListViewModel(rows = rows.flatten)
      } yield Ok(view(list, calculatedDateValue.getOrElse(""), status == UserActiveStatus.Upcoming))
  }

  private def rowGenerator(
    thirdPartyDetails: ThirdPartyDetails,
    maybeBusinessInfo: Option[String],
    thirdPartyEori: String,
    answers: UserAnswers
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    Seq(
      EoriNumberSummary.detailsRow(thirdPartyEori)
    ) ++ (
      (maybeBusinessInfo.isDefined, thirdPartyDetails.referenceName.isDefined) match {
        case (true, true)  =>
          Seq(
            BusinessInfoSummary.row(maybeBusinessInfo.get),
            ThirdPartyReferenceSummary.detailsRow(
              answers
                .get(EditThirdPartyReferencePage(thirdPartyEori))
                .orElse(thirdPartyDetails.referenceName),
              config.editThirdPartyEnabled
            )
          )
        case (true, false) =>
          Seq(BusinessInfoSummary.row(maybeBusinessInfo.get))
        case (false, _)    =>
          Seq(ThirdPartyReferenceSummary.detailsRow(thirdPartyDetails.referenceName, config.editThirdPartyEnabled))
      }
    )
      ++ Seq(
        ThirdPartyAccessPeriodSummary.detailsRow(thirdPartyDetails, config.editThirdPartyEnabled),
        DataTypesSummary.detailsRow(
          answers
            .get(EditThirdPartyDataTypesPage(thirdPartyEori))
            .map(_.map(_.toString))
            .getOrElse(thirdPartyDetails.dataTypes),
          config.editThirdPartyEnabled,
          thirdPartyEori
        ),
        DataTheyCanViewSummary.detailsRow(thirdPartyDetails, config.editThirdPartyEnabled)
      )

  private def resolveDisplayName(companyInfo: CompanyInformation): Option[String] =
    companyInfo.consent match {
      case ConsentStatus.Denied => None
      case _                    => Some(companyInfo.name)
    }
}
