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
import forms.thirdparty.EoriNumberFormProvider
import models.Mode
import models.thirdparty.AddThirdPartySection
import navigation.ThirdPartyNavigator
import pages.thirdparty.{EoriNumberPage, ThirdPartyDataOwnerConsentPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.EoriNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EoriNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ThirdPartyNavigator,
  addThirdPartySection: AddThirdPartySection,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EoriNumberFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: EoriNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userEori = request.eori
    val form     = formProvider(userEori)

    val preparedForm = request.userAnswers.get(EoriNumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val userEori = request.eori
      val form     = formProvider(userEori)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          eori =>
            tradeReportingExtractsService.getAuthorisedEoris(userEori).flatMap { authorisedEoris =>
              if (authorisedEoris.contains(eori)) {
                Future.successful(
                  Redirect(controllers.thirdparty.routes.EoriAlreadyAddedController.onPageLoad())
                    .flashing("alreadyAddedEori" -> eori)
                )
              } else {
                tradeReportingExtractsService.getCompanyInformation(eori).flatMap { companyInfo =>
                  if (companyInfo.name.isEmpty) {
                    val formWithApiError = form.withError("value", "eoriNumber.error.notFound")
                    Future.successful(BadRequest(view(formWithApiError, mode)))
                  } else {
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.set(EoriNumberPage, eori))
                      redirectUrl     = navigator.nextPage(EoriNumberPage, mode, updatedAnswers).url
                      answersWithNav  = addThirdPartySection.saveNavigation(updatedAnswers, redirectUrl)
                      _              <- sessionRepository.set(answersWithNav)
                    } yield Redirect(navigator.nextPage(EoriNumberPage, mode, answersWithNav))
                  }
                }
              }
            }
        )
  }
}
