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

import controllers.actions.*
import forms.thirdparty.DeclarationDateFormProvider
import models.Mode
import models.thirdparty.{AddThirdPartySection, DataTypes}
import pages.editThirdParty.{EditDataEndDatePage, EditDataStartDatePage, EditDeclarationDatePage}
import navigation.{EditThirdPartyNavigator, Navigator, ThirdPartyNavigator}
import pages.thirdparty.{DataTypesPage, DeclarationDatePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.editThirdParty.EditDeclarationDateView
import models.thirdparty.DeclarationDate
import forms.editThirdParty.EditDeclarationDateFormProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditDeclarationDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  editThirdPartyNavigator: EditThirdPartyNavigator,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EditDeclarationDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: EditDeclarationDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService
        .getThirdPartyDetails(request.eori, thirdPartyEori)
        .flatMap { thirdPartyDetails =>
          val dataTypeObjects: Set[DataTypes] = thirdPartyDetails.dataTypes.collect {
            case "imports" => DataTypes.Import
            case "exports" => DataTypes.Export
          }
          val dataTypesString                 = getDataTypesString(dataTypeObjects)
          val form                            = formProvider(Seq(dataTypesString))

          val declartionDateObject: DeclarationDate = thirdPartyDetails.dataStartDate.isEmpty match {
            case true  => DeclarationDate.AllAvailableData
            case false => DeclarationDate.CustomDateRange
          }

          val preparedForm = request.userAnswers.get(EditDeclarationDatePage(thirdPartyEori)) match {
            case None        => form.fill(declartionDateObject)
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, thirdPartyEori, dataTypesString)))
        }
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService
        .getThirdPartyDetails(request.eori, thirdPartyEori)
        .flatMap { thirdPartyDetails =>

          val dataTypeObjects: Set[DataTypes] = thirdPartyDetails.dataTypes.collect {
            case "imports" => DataTypes.Import
            case "exports" => DataTypes.Export
          }
          val dataTypesString                 = getDataTypesString(dataTypeObjects)

          val form = formProvider(Seq(dataTypesString))

          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, thirdPartyEori, dataTypesString))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(EditDeclarationDatePage(thirdPartyEori), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  editThirdPartyNavigator
                    .nextPage(EditDeclarationDatePage(thirdPartyEori), userAnswers = updatedAnswers)
                )
            )
        }
  }

  def getDataTypesString(dataTypesAnswer: Set[DataTypes])(implicit messages: Messages): String =
    dataTypesAnswer match {
      case set if set == Set(DataTypes.Import)                   => messages("declarationDate.import")
      case set if set == Set(DataTypes.Export)                   => messages("declarationDate.export")
      case set if set == Set(DataTypes.Import, DataTypes.Export) => messages("declarationDate.importExport")
    }

}
