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
import forms.thirdparty.DeclarationDateFormProvider
import models.Mode
import models.thirdparty.{AddThirdPartySection, DataTypes}
import navigation.ThirdPartyNavigator
import pages.thirdparty.{DataTypesPage, DeclarationDatePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.DeclarationDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  thirdPartyNavigator: ThirdPartyNavigator,
  addThirdPartySection: AddThirdPartySection,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeclarationDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclarationDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val dataTypesString = getDataTypesString(request.userAnswers.get(DataTypesPage))
    val form            = formProvider(Seq(dataTypesString))

    val preparedForm = request.userAnswers.get(DeclarationDatePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, dataTypesString))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val dataTypesString = getDataTypesString(request.userAnswers.get(DataTypesPage))
      val form            = formProvider(Seq(dataTypesString))

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, dataTypesString))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DeclarationDatePage, value))
              redirectUrl     = thirdPartyNavigator.nextPage(DeclarationDatePage, mode, updatedAnswers).url
              answersWithNav  = addThirdPartySection.saveNavigation(updatedAnswers, redirectUrl)
              _              <- sessionRepository.set(answersWithNav)
            } yield Redirect(thirdPartyNavigator.nextPage(DeclarationDatePage, mode, updatedAnswers))
        )
  }

  def getDataTypesString(dataTypesAnswer: Option[Set[DataTypes]])(implicit messages: Messages): String =
    dataTypesAnswer match {
      case Some(set) if set == Set(DataTypes.Import)                   => messages("declarationDate.import")
      case Some(set) if set == Set(DataTypes.Export)                   => messages("declarationDate.export")
      case Some(set) if set == Set(DataTypes.Import, DataTypes.Export) => messages("declarationDate.importExport")
      case _                                                           => ""
    }
}
