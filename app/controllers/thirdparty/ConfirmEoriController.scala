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
import forms.thirdparty.ConfirmEoriFormProvider
import models.{CompanyInformation, ConsentStatus, Mode}
import models.thirdparty.ConfirmEori
import navigation.Navigator
import pages.ConfirmEoriPage
import pages.thirdparty.EoriNumberPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.thirdparty.ConfirmEoriView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmEoriController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConfirmEoriFormProvider,
  tradeReportingExtractsService: TradeReportingExtractsService,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(ConfirmEoriPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(EoriNumberPage) match {
        case Some(eoriNumber) =>
          tradeReportingExtractsService.getCompanyInformation(eoriNumber).map { businessInfo =>
            Ok(view(preparedForm, mode, eoriNumber, resolveDisplayName(businessInfo)))
          }

        case None =>
          Future.successful(Redirect(routes.EoriNumberController.onPageLoad(mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(EoriNumberPage) match {
        case Some(eoriNumber) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                tradeReportingExtractsService.getCompanyInformation(eoriNumber).map { businessInfo =>
                  BadRequest(view(formWithErrors, mode, eoriNumber, resolveDisplayName(businessInfo)))
                },
              {
                case ConfirmEori.No =>
                  for {
                    updatedAnswers <- Future.fromTry(
                                        request.userAnswers
                                          .remove(EoriNumberPage)
                                          .flatMap(_.remove(ConfirmEoriPage))
                                      )
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(routes.EoriNumberController.onPageLoad(mode))

                case confirmValue =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmEoriPage, confirmValue))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(ConfirmEoriPage, mode, updatedAnswers))
              }
            )

        case None =>
          Future.successful(Redirect(routes.EoriNumberController.onPageLoad(mode)))
      }
  }

  private def resolveDisplayName(companyInfo: CompanyInformation)(implicit messages: Messages): String =
    companyInfo.consent match {
      case ConsentStatus.Denied => messages("confirmEori.noConsent")
      case _                    => companyInfo.name
    }

}
