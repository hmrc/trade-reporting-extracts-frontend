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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAllowListConnector
import controllers.actions.utils.Retrievals.Ops
import controllers.problem.routes
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  "Auth Action" - {

    "when authorised retrieval returns a valid enrolment" - {

      "must call the block and return OK" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
          val appConfig     = application.injector.instanceOf[FrontendAppConfig]
          val mockConnector = mock[UserAllowListConnector]

          val enrolment  = Enrolment(
            appConfig.cdsEnrolmentIdentifier.key,
            Seq(EnrolmentIdentifier(appConfig.cdsEnrolmentIdentifier.identifier, "some-eori")),
            "Activated"
          )
          val enrolments = Enrolments(Set(enrolment))

          val retrieval = Some("internal-id") ~ Some(AffinityGroup.Individual) ~ Some(User) ~ enrolments

          val authAction = new AuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(retrieval),
            mockConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
        }
      }

      "when authorised retrieval returns an enrolment with empty identifier" - {

        "must redirect to the unauthorised page" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
            val appConfig     = application.injector.instanceOf[FrontendAppConfig]
            val mockConnector = mock[UserAllowListConnector]

            val enrolment  = Enrolment(
              appConfig.cdsEnrolmentIdentifier.key,
              Seq(EnrolmentIdentifier(appConfig.cdsEnrolmentIdentifier.identifier, "")),
              "Activated"
            )
            val enrolments = Enrolments(Set(enrolment))

            val retrieval = Some("internal-id") ~ Some(AffinityGroup.Individual) ~ None ~ enrolments

            val authAction = new AuthenticatedIdentifierAction(
              new FakeSuccessfulAuthConnector(retrieval),
              mockConnector,
              appConfig,
              bodyParsers
            )
            val controller = new Harness(authAction)
            val result     = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
          }
        }

        "when authorised retrieval returns no CDS enrolment" - {

          "must redirect to the unauthorised page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]

              val enrolments = Enrolments(Set.empty)
              val retrieval  = Some("internal-id") ~ Some(AffinityGroup.Individual) ~ None ~ enrolments

              val authAction = new AuthenticatedIdentifierAction(
                new FakeSuccessfulAuthConnector(retrieval),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller = new Harness(authAction)
              val result     = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
            }
          }
        }

        "when user allow list is enabled and the connector returns true" - {
          "must call the block and return OK" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val realAppConfig = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]

              val mockConfig = mock[FrontendAppConfig]
              when(mockConfig.cdsEnrolmentIdentifier).thenReturn(realAppConfig.cdsEnrolmentIdentifier)
              when(mockConfig.userAllowListEnabled).thenReturn(true)
              when(mockConfig.userAllowListFeature).thenReturn(realAppConfig.userAllowListFeature)

              when(mockConnector.check(any(), any())(any())).thenReturn(Future.successful(true))

              val enrolment  = Enrolment(
                realAppConfig.cdsEnrolmentIdentifier.key,
                Seq(EnrolmentIdentifier(realAppConfig.cdsEnrolmentIdentifier.identifier, "some-eori")),
                "Activated"
              )
              val enrolments = Enrolments(Set(enrolment))
              val retrieval  = Some("internal-id") ~ Some(AffinityGroup.Individual) ~ None ~ enrolments

              val authAction = new AuthenticatedIdentifierAction(
                new FakeSuccessfulAuthConnector(retrieval),
                mockConnector,
                mockConfig,
                bodyParsers
              )
              val controller = new Harness(authAction)
              val result     = controller.onPageLoad()(FakeRequest())

              status(result) mustBe OK
            }
          }
        }

        "when user allow list is enabled and the connector returns false" - {
          "must redirect to the NoPermission page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val realAppConfig = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]

              val mockConfig = mock[FrontendAppConfig]
              when(mockConfig.cdsEnrolmentIdentifier).thenReturn(realAppConfig.cdsEnrolmentIdentifier)
              when(mockConfig.userAllowListEnabled).thenReturn(true)
              when(mockConfig.userAllowListFeature).thenReturn(realAppConfig.userAllowListFeature)

              when(mockConnector.check(any(), any())(any())).thenReturn(Future.successful(false))

              val enrolment  = Enrolment(
                realAppConfig.cdsEnrolmentIdentifier.key,
                Seq(EnrolmentIdentifier(realAppConfig.cdsEnrolmentIdentifier.identifier, "some-eori")),
                "Activated"
              )
              val enrolments = Enrolments(Set(enrolment))
              val retrieval  = Some("internal-id") ~ Some(AffinityGroup.Individual) ~ None ~ enrolments

              val authAction = new AuthenticatedIdentifierAction(
                new FakeSuccessfulAuthConnector(retrieval),
                mockConnector,
                mockConfig,
                bodyParsers
              )
              val controller = new Harness(authAction)
              val result     = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe controllers.problem.routes.NoPermissionController.onPageLoad().url
            }
          }
        }

        "when the user hasn't logged in" - {

          "must redirect the user to log in " in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]

              val authAction = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new MissingBearerToken),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller = new Harness(authAction)
              val result     = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value must startWith(appConfig.loginUrl)
            }
          }
        }

        "the user's session has expired" - {

          "must redirect the user to log in " in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]

              val authAction = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new BearerTokenExpired),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller = new Harness(authAction)
              val result     = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value must startWith(appConfig.loginUrl)
            }
          }
        }

        "the user doesn't have sufficient enrolments" - {

          "must redirect the user to the unauthorised page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]
              val authAction    = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new InsufficientEnrolments),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller    = new Harness(authAction)
              val result        = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
            }
          }
        }

        "the user doesn't have sufficient confidence level" - {

          "must redirect the user to the unauthorised page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]
              val authAction    = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller    = new Harness(authAction)
              val result        = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
            }
          }
        }

        "the user used an unaccepted auth provider" - {

          "must redirect the user to the unauthorised page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]
              val authAction    = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new UnsupportedAuthProvider),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller    = new Harness(authAction)
              val result        = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
            }
          }
        }

        "the user has an unsupported affinity group" - {

          "must redirect the user to the unauthorised page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]
              val authAction    = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller    = new Harness(authAction)
              val result        = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(routes.UnsupportedAffinityGroupController.onPageLoad().url)
            }
          }
        }

        "the user has an unsupported credential role" - {

          "must redirect the user to the unauthorised page" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val bodyParsers   = application.injector.instanceOf[BodyParsers.Default]
              val appConfig     = application.injector.instanceOf[FrontendAppConfig]
              val mockConnector = mock[UserAllowListConnector]
              val authAction    = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new UnsupportedCredentialRole),
                mockConnector,
                appConfig,
                bodyParsers
              )
              val controller    = new Harness(authAction)
              val result        = controller.onPageLoad()(FakeRequest())

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
            }
          }
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}

class FakeSuccessfulAuthConnector @Inject() (retrievalToReturn: Any) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.successful(retrievalToReturn.asInstanceOf[A])
}
