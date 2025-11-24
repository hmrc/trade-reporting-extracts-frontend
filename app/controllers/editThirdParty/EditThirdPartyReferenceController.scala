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
import forms.editThirdParty.EditThirdPartyReferenceFormProvider
import models.Mode
import navigation.EditThirdPartyNavigator
import pages.editThirdParty.EditThirdPartyReferencePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.editThirdParty.EditThirdPartyReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EditThirdPartyReferenceController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  editThirdPartyNavigator: EditThirdPartyNavigator,
  formProvider: EditThirdPartyReferenceFormProvider,
  val controllerComponents: MessagesControllerComponents,
  tradeReportingExtractsService: TradeReportingExtractsService,
  view: EditThirdPartyReferenceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(thirdPartyEori: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      tradeReportingExtractsService
        .getThirdPartyDetails(request.eori, thirdPartyEori)
        .flatMap { thirdPartyDetails =>
          val preparedForm = request.userAnswers.get(EditThirdPartyReferencePage(thirdPartyEori)) match {
            case None        =>
              form.fill(thirdPartyDetails.referenceName.get)
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, thirdPartyEori)))
        }
        .recover { case ex =>
          Redirect(controllers.routes.DashboardController.onPageLoad())
        }
  }

  def onSubmit(thirdPartyEori: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      tradeReportingExtractsService.getThirdPartyDetails(request.eori, thirdPartyEori).flatMap { thirdPartyDetails =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, thirdPartyEori))),
            value => {
              val originalReferenceName  = thirdPartyDetails.referenceName.getOrElse("").trim
              val submittedReferenceName = value.trim

              if (submittedReferenceName.equalsIgnoreCase(originalReferenceName)) {
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.remove(EditThirdPartyReferencePage(thirdPartyEori)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  editThirdPartyNavigator
                    .nextPage(EditThirdPartyReferencePage(thirdPartyEori), userAnswers = updatedAnswers)
                )
              } else {
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers.set(EditThirdPartyReferencePage(thirdPartyEori), submittedReferenceName)
                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  editThirdPartyNavigator
                    .nextPage(EditThirdPartyReferencePage(thirdPartyEori), userAnswers = updatedAnswers)
                )
              }
            }
          )
      }
    }

}
