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

import controllers.actions.*
import forms.report.ReportDateRangeFormProvider
import models.{CheckMode, Mode}
import models.report.ReportDateRange
import navigation.ReportNavigator
import pages.report.{CustomRequestEndDatePage, CustomRequestStartDatePage, ReportDateRangePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.report.ReportDateRangeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class ReportDateRangeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReportDateRangeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReportDateRangeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[ReportDateRange] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(ReportDateRangePage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val updatedAnswersTry = for {
              withDateRange  <- request.userAnswers.set(ReportDateRangePage, value)
              cleanedAnswers <- value match {
                                  case ReportDateRange.CustomDateRange =>
                                    // Keep existing custom date answers
                                    scala.util.Success(withDateRange)

                                  case _ =>
                                    // Clear custom date answers in both Normal and Check modes
                                    withDateRange
                                      .remove(CustomRequestStartDatePage)
                                      .flatMap(_.remove(CustomRequestEndDatePage))
                                }
            } yield cleanedAnswers

            updatedAnswersTry match {
              case scala.util.Success(updatedAnswers) =>
                for {
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(ReportDateRangePage, mode, updatedAnswers))

              case scala.util.Failure(_) =>
                Future.successful(Redirect(controllers.problem.routes.JourneyRecoveryController.onPageLoad()))
            }
          }
        )
  }

}
