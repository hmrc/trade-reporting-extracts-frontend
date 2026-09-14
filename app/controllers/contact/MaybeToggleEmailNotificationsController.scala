/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.contact

import controllers.actions.*
import forms.contact.MaybeToggleEmailNotificationsFormProvider
import models.{Mode, UpdateEmailPreference}
import navigation.Navigator
import pages.contact.MaybeToggleEmailNotificationsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TradeReportingExtractsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.contact.MaybeToggleEmailNotificationsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MaybeToggleEmailNotificationsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  getOrCreate: DataRetrievalOrCreateAction,
  tradeReportingExtractsService: TradeReportingExtractsService,
  formProvider: MaybeToggleEmailNotificationsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MaybeToggleEmailNotificationsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(preference: Boolean): Action[AnyContent] = (identify andThen getOrCreate).async { implicit request =>
    tradeReportingExtractsService.getUserDetails(request.eori).map { userDetails =>
      Ok(view(form, preference, userDetails.notificationEmail.address))
    }

  }

  def onSubmit(preference: Boolean): Action[AnyContent] = (identify andThen getOrCreate).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          tradeReportingExtractsService.getUserDetails(request.eori).map { userDetails =>
            BadRequest(view(formWithErrors, preference, userDetails.notificationEmail.address))
          },
        value =>
          if (value) {
            tradeReportingExtractsService
              .updatePersonalEmailNotificationsPreference(UpdateEmailPreference(request.eori, preference)) map { _ =>
              Redirect(
                controllers.contact.routes.ToggledEmailNotificationsConfirmationController.onPageLoad(preference)
              )
            }
          } else {
            Future.successful(Redirect(controllers.contact.routes.ContactDetailsController.onPageLoad()))
          }
      )
  }

}
