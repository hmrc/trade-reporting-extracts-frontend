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

package controllers.report

import controllers.BaseController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import views.html.report.DecisionView
import play.api.mvc.{Action, AnyContent}

import javax.inject.Inject
import play.api.mvc.MessagesControllerComponents
import forms.report.DecisionFormProvider
import models.Mode
import navigation.ReportNavigator
import pages.report.DecisionPage


class DecisionController @Inject()(
  identify: IdentifierAction,
  view: DecisionView,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DecisionFormProvider,
  navigator: ReportNavigator,
  val controllerComponents: MessagesControllerComponents
  ) extends BaseController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DecisionPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(view(formWithErrors,mode))
        },
      value => {
        Redirect(navigator.nextPage(DecisionPage, mode, request.userAnswers))
      }
    )
  }
  
}