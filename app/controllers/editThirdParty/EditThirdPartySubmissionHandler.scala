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
import models.thirdparty.ThirdPartyRequest
import pages.editThirdParty.{EditThirdPartyAccessEndDatePage, EditThirdPartyAccessStartDatePage, EditThirdPartyDataTypesPage, EditThirdPartyReferencePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import utils.UserAnswerHelper
import utils.DateTimeFormats.localDateToInstant

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
          accessEnd = (
            request.userAnswers.get(EditThirdPartyAccessEndDatePage(thirdPartyEori)),
            previousDetails.accessEndDate
          ) match {
            case (Some(value), _) if value == LocalDate.MAX =>
              None
            case (Some(value), _)                           => Some(localDateToInstant(value))
            case (None, Some(prevValue))                    => Some(localDateToInstant(prevValue))
            case (_, _)                                     => None
          },
          // NEED PAGES
          reportDateStart = None,
          reportDateEnd = None,
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
            // TODO RECOVER SOMEWHERE BETTER?
            Redirect(controllers.routes.DashboardController.onPageLoad())
          }
      }
    }
}
