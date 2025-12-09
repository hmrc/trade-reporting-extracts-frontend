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
import forms.report.ChooseEoriFormProvider
import models.Mode
import models.report.{ChooseEori, ReportDateRange, ReportRequestSection}
import navigation.ReportNavigator
import pages.QuestionPage
import pages.report.*
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import views.html.report.ChooseEoriView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChooseEoriController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: ReportNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ChooseEoriFormProvider,
  reportRequestSection: ReportRequestSection,
  view: ChooseEoriView,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ChooseEoriPage).fold(form)(form.fill)

      Ok(view(preparedForm, mode, request.eori))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val prevAnswer = request.userAnswers.get(ChooseEoriPage)

      val pagesToRemove: Seq[QuestionPage[_]] = Seq(
        DecisionPage,
        SelectThirdPartyEoriPage,
        EoriRolePage,
        ReportTypeImportPage,
        ReportDateRangePage,
        CustomRequestStartDatePage,
        CustomRequestEndDatePage,
        ReportNamePage,
        MaybeAdditionalEmailPage,
        EmailSelectionPage,
        NewEmailNotificationPage
      )

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.eori))),
          value =>
            if (prevAnswer.contains(value) && prevAnswer.contains(ChooseEori.Myeori)) {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ChooseEoriPage, value))
                redirectUrl     = navigator.nextPage(ChooseEoriPage, mode, updatedAnswers).url
                answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                _              <- sessionRepository.set(answersWithNav)
              } yield Redirect(navigator.nextPage(ChooseEoriPage, mode, answersWithNav))
            } else {
              for {
                answers        <- Future.fromTry(request.userAnswers.set(ChooseEoriPage, value))
                cleanedAnswers <- pagesToRemove.foldLeft(Future.successful(answers)) { (acc, page) =>
                                    acc.flatMap(ans => Future.fromTry(ans.remove(page)))
                                  }
                updatedAnswers <- if (value == ChooseEori.Myauthority) {
                                    Future.fromTry(
                                      cleanedAnswers.set(ReportDateRangePage, ReportDateRange.CustomDateRange)
                                    )
                                  } else Future.successful(cleanedAnswers)
                redirectUrl     = navigator.nextPage(ChooseEoriPage, mode, updatedAnswers).url
                answersWithNav  = reportRequestSection.saveNavigation(updatedAnswers, redirectUrl)
                _              <- sessionRepository.set(answersWithNav)
              } yield Redirect(navigator.nextPage(ChooseEoriPage, mode, answersWithNav))
            }
        )
    }
}
