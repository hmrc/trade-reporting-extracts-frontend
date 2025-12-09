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

package controllers.editThirdParty

import com.google.inject.Inject
import controllers.BaseController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.ThirdPartyDetails
import models.requests.DataRequest
import models.thirdparty.{DeclarationDate, ThirdPartyRequest}
import pages.editThirdParty.{EditDataEndDatePage, EditDataStartDatePage, EditDeclarationDatePage, EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage, EditThirdPartyDataTypesPage, EditThirdPartyReferencePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import utils.UserAnswerHelper
import utils.DateTimeFormats.localDateToInstant
import utils.json.OptionalLocalDateReads.*

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext

class EditThirdPartySubmissionHandler @Inject (
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  tradeReportingExtractsService: TradeReportingExtractsService,
  userAnswerHelper: UserAnswerHelper,
  sessionRepository: SessionRepository
)(implicit val ec: ExecutionContext)
    extends BaseController
    with I18nSupport {

  def submit(thirdPartyEori: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap { previousDetails =>

        val updatedDetails = ThirdPartyRequest(
          userEORI = request.eori,
          thirdPartyEORI = thirdPartyEori,
          accessStart = localDateToInstant(
            request.userAnswers
              .get(EditThirdPartyAccessStartDatePage(thirdPartyEori))
              .getOrElse(previousDetails.accessStartDate)
          ),
          accessEnd = determineAccessEndDate(thirdPartyEori, request, previousDetails),
          reportDateStart = determineReportDateStart(thirdPartyEori, request, previousDetails),
          reportDateEnd = determineReportDateEnd(thirdPartyEori, request, previousDetails),
          accessType = request.userAnswers
            .get(EditThirdPartyDataTypesPage(thirdPartyEori))
            .map(_.map(_.toString.toUpperCase()))
            .getOrElse(previousDetails.dataTypes),
          referenceName =
            request.userAnswers.get(EditThirdPartyReferencePage(thirdPartyEori)).orElse(previousDetails.referenceName)
        )

        (for {
          _             <- tradeReportingExtractsService.editThirdPartyRequest(updatedDetails)
          updatedAnswers = userAnswerHelper.removeEditThirdPartyAnswersForEori(thirdPartyEori, request.userAnswers)
          _             <- sessionRepository.set(updatedAnswers)
        } yield Redirect(controllers.thirdparty.routes.AuthorisedThirdPartiesController.onPageLoad()))
          .recover { case ex =>
            logger.error(s"Error submitting edit third party: ${ex.getMessage}", ex)
            // TODO RECOVER SOMEWHERE BETTER?
            Redirect(controllers.routes.DashboardController.onPageLoad())
          }
      }
    }

  private def determineAccessEndDate(
    thirdPartyEori: String,
    request: DataRequest[AnyContent],
    previousDetails: ThirdPartyDetails
  ) =
    (
      request.userAnswers.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)),
      previousDetails.accessEndDate
    ) match {
      case (Some(value), _) if value == LocalDate.MAX =>
        None
      case (Some(value), _)                           => Some(localDateToInstant(value))
      case (None, Some(prevValue))                    => Some(localDateToInstant(prevValue))
      case (_, _)                                     => None
    }

  private def determineReportDateStart(
    thirdPartyEori: String,
    request: DataRequest[AnyContent],
    previousDetails: ThirdPartyDetails
  ) =
    (
      request.userAnswers.get(EditDeclarationDatePage(thirdPartyEori)),
      previousDetails.dataStartDate,
      request.userAnswers.get(EditDataStartDatePage(thirdPartyEori))
    ) match {
      case (Some(DeclarationDate.AllAvailableData), _, _) => None
      case (_, _, Some(value))                            => Some(localDateToInstant(value))
      case (_, Some(prevValue), _)                        => Some(localDateToInstant(prevValue))
      case (_, _, _)                                      => None
    }

  private def determineReportDateEnd(
    thirdPartyEori: String,
    request: DataRequest[AnyContent],
    previousDetails: ThirdPartyDetails
  ) =
    (
      request.userAnswers.get(EditDataEndDatePage(thirdPartyEori)),
      previousDetails.dataEndDate,
      request.userAnswers.get(EditDeclarationDatePage(thirdPartyEori))
    ) match {
      case (_, _, Some(DeclarationDate.AllAvailableData))      =>
        None
      case (Some(Some(value)), _, _) if value == LocalDate.MAX =>
        None
      case (Some(Some(value)), _, _)                           => Some(localDateToInstant(value))
      case (None, Some(prevValue), _)                          => Some(localDateToInstant(prevValue))
      case (_, _, _)                                           => None
    }
}
