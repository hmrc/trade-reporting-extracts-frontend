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
import models.{CompanyInformation, ConsentStatus, UserAnswers}
import pages.thirdparty.EoriNumberPage

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.thirdparty.{BusinessInfoSummary, DataTheyCanViewSummary, DataTypesSummary, DeclarationDateSummary, EoriNumberSummary, ThirdPartyAccessPeriodSummary, ThirdPartyDataOwnerConsentSummary, ThirdPartyReferenceSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.thirdparty.AddThirdPartyCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class AddThirdPartyCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: AddThirdPartyCheckYourAnswersView,
  tradeReportingExtractsService: TradeReportingExtractsService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    val userAnsewrs = request.userAnswers

    for {
      companyInfo     <- tradeReportingExtractsService.getCompanyInformation(userAnsewrs.get(EoriNumberPage).get)
      maybeCompanyName = resolveDisplayName(companyInfo)
      rows             = rowGenerator(userAnsewrs, maybeCompanyName)
      list             = SummaryListViewModel(rows = rows.flatten)
    } yield Ok(view(list))
  }

  private def rowGenerator(answers: UserAnswers, maybeBusinessInfo: Option[String])(implicit
    messages: Messages
  ): Seq[Option[SummaryListRow]] =
    Seq(
      ThirdPartyDataOwnerConsentSummary.row(answers),
      EoriNumberSummary.checkYourAnswersRow(answers),
      if (maybeBusinessInfo.isDefined) {
        BusinessInfoSummary.row(maybeBusinessInfo.get)
      } else {
        ThirdPartyReferenceSummary.checkYourAnswersRow(answers)
      },
      ThirdPartyAccessPeriodSummary.checkYourAnswersRow(answers),
      DataTypesSummary.checkYourAnswersRow(answers),
      DeclarationDateSummary.row(answers),
      DataTheyCanViewSummary.checkYourAnswersRow(answers)
    )

  private def resolveDisplayName(companyInfo: CompanyInformation): Option[String] =
    companyInfo.consent match {
      case ConsentStatus.Denied => None
      case _                    => Some(companyInfo.name)
    }
}
