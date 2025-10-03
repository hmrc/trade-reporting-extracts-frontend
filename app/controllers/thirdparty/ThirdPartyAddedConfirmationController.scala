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
import models.requests.DataRequest
import models.thirdparty.DataTypes.{Export, Import}
import models.{AlreadyAddedThirdPartyFlag, CompanyInformation, ConsentStatus}
import models.thirdparty.{AddThirdPartySection, ConfirmEori, DataTypes, DeclarationDate, ThirdPartyAddedEvent}
import pages.thirdparty.{ConfirmEoriPage, DataEndDatePage, DataStartDatePage, DataTypesPage, DeclarationDatePage, EoriNumberPage, ThirdPartyAccessEndDatePage, ThirdPartyAccessStartDatePage, ThirdPartyDataOwnerConsentPage, ThirdPartyReferencePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, ThirdPartyService, TradeReportingExtractsService}
import utils.DateTimeFormats.dateTimeFormat
import views.html.thirdparty.ThirdPartyAddedConfirmationView
import utils.json.OptionalLocalDateReads.*

import java.time.{Clock, LocalDate, ZoneOffset}
import javax.inject.Inject
import scala.collection.immutable.{AbstractSet, SortedSet}
import scala.concurrent.{ExecutionContext, Future}

class ThirdPartyAddedConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  thirdPartyService: ThirdPartyService,
  requireData: DataRequiredAction,
  frontendAppConfig: FrontendAppConfig,
  sessionRepository: SessionRepository,
  tradeReportingExtractsService: TradeReportingExtractsService,
  auditService: AuditService,
  preventBackNavigationAfterAddThirdPartyAction: PreventBackNavigationAfterAddThirdPartyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ThirdPartyAddedConfirmationView,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends BaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify
    andThen getData
    andThen requireData
    andThen preventBackNavigationAfterAddThirdPartyAction).async { implicit request =>
    for {
      thirdPartyAddedConfirmation      <- tradeReportingExtractsService.createThirdPartyAddRequest(
                                            thirdPartyService.buildThirdPartyAddRequest(request.userAnswers, request.eori)
                                          )
      companyInfo                      <- tradeReportingExtractsService.getCompanyInformation(request.userAnswers.get(EoriNumberPage).get)
      maybeCompanyName                  = resolveDisplayName(companyInfo)
      _                                <- auditService.auditThirdPartyAdded(buildThirdPartyAddedAuditEvent(request, maybeCompanyName))
      updatedAnswers                    = AddThirdPartySection.removeAllAddThirdPartyAnswersAndNavigation(request.userAnswers)
      updatedAnswersWithSubmissionFlag <- Future.fromTry(updatedAnswers.set(AlreadyAddedThirdPartyFlag(), true))
      _                                <- sessionRepository.set(updatedAnswersWithSubmissionFlag)
    } yield Ok(view(thirdPartyAddedConfirmation.thirdPartyEori, getDate, frontendAppConfig.exitSurveyUrl))
  }

  private def getDate(implicit messages: Messages): String =
    LocalDate.now(clock).format(dateTimeFormat()(messages.lang))

  private def resolveDisplayName(companyInfo: CompanyInformation): Option[String] =
    companyInfo.consent match {
      case ConsentStatus.Denied => None
      case _                    => Some(companyInfo.name)
    }

  private def buildThirdPartyAddedAuditEvent(
    request: DataRequest[AnyContent],
    maybeCompanyName: Option[String]
  ): ThirdPartyAddedEvent = {
    val userAnswers = request.userAnswers
    
    ThirdPartyAddedEvent(
      IsImporterExporterForDataToShare = userAnswers.get(ThirdPartyDataOwnerConsentPage).get,
      thirdPartyEoriAccessGiven = userAnswers.get(ConfirmEoriPage).get match {
        case ConfirmEori.Yes => true
        case ConfirmEori.No  => false
      },
      thirdPartyGivenAccessAllData = userAnswers.get(DeclarationDatePage).get match {
        case DeclarationDate.AllAvailableData => true
        case DeclarationDate.CustomDateRange  => false
      },
      requesterEori = request.eori,
      thirdPartyEori = userAnswers.get(EoriNumberPage).get,
      thirdPartyBusinessInformation = maybeCompanyName,
      thirdPartyReferenceName = userAnswers.get(ThirdPartyReferencePage),
      thirdPartyAccessStart =
        userAnswers.get(ThirdPartyAccessStartDatePage).get.atStartOfDay().toInstant(ZoneOffset.UTC).toString,
      thirdPartyAccessEnd = userAnswers.get(ThirdPartyAccessEndDatePage) match {
        case Some(Some(endDate)) => endDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString
        case _                   => "indefinite"
      },
      dataAccessType = userAnswers.get(DataTypesPage).get match {
        case set if set.contains(DataTypes.Export) && set.contains(DataTypes.Import) => "import, export"
        case set if set.contains(DataTypes.Export)                                   => "export"
        case _                                                                       => "import"
      },
      thirdPartyDataStart = userAnswers.get(DataStartDatePage) match {
        case None            => "all available data"
        case Some(startDate) => startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString
      },
      thirdPartyDataEnd = userAnswers.get(DataEndDatePage) match {
        case Some(Some(endDate)) => endDate.atStartOfDay().toInstant(ZoneOffset.UTC).toString
        case _                   => "all available data"
      }
    )
  }
}
