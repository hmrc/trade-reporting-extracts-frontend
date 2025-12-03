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
import models.thirdparty.DataTypes
import models.thirdparty.DataTypes.*
import models.{CompanyInformation, ConsentStatus, ThirdPartyDetails, UserActiveStatus, UserAnswers}
import pages.editThirdParty.*
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TradeReportingExtractsService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats
import utils.DateTimeFormats.computeCalculatedDateValue
import viewmodels.checkAnswers.thirdparty.*
import viewmodels.govuk.all.SummaryListViewModel
import views.html.thirdparty.ThirdPartyDetailsView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ThirdPartyDetailsController @Inject() (
  clock: Clock = Clock.systemUTC(),
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  getOrCreate: DataRetrievalOrCreateAction,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyDetailsView,
  tradeReportingExtractsService: TradeReportingExtractsService,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends BaseController
    with I18nSupport {

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify  andThen getOrCreate).async {
    implicit request =>
      for {
        companyInfo       <- tradeReportingExtractsService.getCompanyInformation(thirdPartyEori)
        maybeCompanyName   = resolveDisplayName(companyInfo)
        thirdPartyDetails <- tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori)

        status = UserActiveStatus.fromInstants(
                   thirdPartyDetails.accessStartDate.atStartOfDay(clock.getZone).toInstant,
                   thirdPartyDetails.dataStartDate.map(_.atStartOfDay(clock.getZone).toInstant),
                   clock
                 )

        calculatedDateValue = computeCalculatedDateValue(thirdPartyDetails, status)

        (rows, hasChangesFlag) = rowGenerator(thirdPartyDetails, maybeCompanyName, thirdPartyEori, request.userAnswers)

        list = SummaryListViewModel(rows = rows.flatten)
      } yield Ok(view(list, calculatedDateValue.getOrElse(""), status == UserActiveStatus.Upcoming, hasChangesFlag))
  }

  private def rowGenerator(
    thirdPartyDetails: ThirdPartyDetails,
    maybeBusinessInfo: Option[String],
    thirdPartyEori: String,
    answers: UserAnswers
  )(implicit messages: Messages): (Seq[Option[SummaryListRow]], Boolean) = {

    val rows = Seq(
      EoriNumberSummary.detailsRow(thirdPartyEori)
    ) ++ (
      (maybeBusinessInfo.isDefined, thirdPartyDetails.referenceName.isDefined) match {
        case (true, true)  =>
          Seq(
            BusinessInfoSummary.row(maybeBusinessInfo.get),
            ThirdPartyReferenceSummary.detailsRow(
              answers.get(EditThirdPartyReferencePage(thirdPartyEori)).orElse(thirdPartyDetails.referenceName),
              config.editThirdPartyEnabled,
              thirdPartyEori
            )
          )
        case (true, false) =>
          Seq(BusinessInfoSummary.row(maybeBusinessInfo.get))
        case (false, _)    =>
          Seq(
            ThirdPartyReferenceSummary.detailsRow(
              answers.get(EditThirdPartyReferencePage(thirdPartyEori)).orElse(thirdPartyDetails.referenceName),
              config.editThirdPartyEnabled,
              thirdPartyEori
            )
          )
      }
    ) ++ Seq(
      ThirdPartyAccessPeriodSummary
        .detailsRow(thirdPartyDetails, config.editThirdPartyEnabled, thirdPartyEori, answers),
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

    val hasChangesFlag = detectChanges(thirdPartyDetails, answers, thirdPartyEori)
    (rows, hasChangesFlag)
  }

  private def detectChanges(
    thirdPartyDetails: ThirdPartyDetails,
    answers: UserAnswers,
    thirdPartyEori: String
  ): Boolean = buildChecks(thirdPartyDetails, thirdPartyEori).exists { check =>
    answers.get(check.page)(check.reads) match {
      case Some(value) => check.normalize(value) != check.original
      case None        => false
    }
  }

  private def buildChecks(thirdPartyDetails: ThirdPartyDetails, thirdPartyEori: String): Seq[ChangeCheck[_]] = Seq(
    ChangeCheck(
      EditThirdPartyReferencePage(thirdPartyEori),
      (v: String) => v,
      thirdPartyDetails.referenceName.getOrElse(""),
      implicitly[Reads[String]]
    ),
    ChangeCheck(
      EditThirdPartyDataTypesPage(thirdPartyEori),
      (v: Set[DataTypes]) => v.map(_.toString).mkString(","),
      thirdPartyDetails.dataTypes.map(identity).mkString(","),
      implicitly[Reads[Set[DataTypes]]]
    ),
    ChangeCheck(
      EditThirdPartyAccessStartDatePage(thirdPartyEori),
      (v: LocalDate) => v.toString,
      thirdPartyDetails.accessStartDate.toString,
      implicitly[Reads[LocalDate]]
    ),
    ChangeCheck(
      EditThirdPartyAccessEndDatePage(thirdPartyEori),
      (v: LocalDate) => v.toString,
      thirdPartyDetails.accessEndDate.map(_.toString).getOrElse(""),
      implicitly[Reads[LocalDate]]
    )
  )

  private case class ChangeCheck[A](
    page: pages.QuestionPage[A],
    normalize: A => String,
    original: String,
    reads: Reads[A]
  )

  private def resolveDisplayName(companyInfo: CompanyInformation): Option[String] =
    companyInfo.consent match {
      case ConsentStatus.Denied => None
      case _                    => Some(companyInfo.name)
    }
}
