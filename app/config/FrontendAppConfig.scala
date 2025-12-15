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

package config

import com.google.inject.{Inject, Singleton}
import models.EnrolmentConfig
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "trade-reporting-extracts-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String                    = configuration.get[String]("urls.login")
  val loginContinueUrl: String            = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String                  = configuration.get[String]("urls.signOut")
  val signOutContinueUrl: String          = configuration.get[String]("urls.signOutContinue")
  val cdsSubscribeUrl: String             = configuration.get[String]("urls.cdsSubscribeUrl")
  val guidanceWhatsInTheReportUrl: String =
    configuration.get[String]("urls.guidanceWhatsInTheReportUrl")

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/trade-reporting-extracts-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  val editThirdPartyEnabled: Boolean =
    configuration.get[Boolean]("features.edit-third-party")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val thirdPartyEnabled: Boolean =
    configuration.get[Boolean]("features.third-party")

  val notificationsEnabled: Boolean =
    configuration.get[Boolean]("features.notifications")

  val userAllowListEnabled: Boolean = configuration.get[Boolean]("features.user-allow-list")
  val userAllowListFeature: String  = configuration.get[String]("features.resource-feature")

  lazy val tradeReportingExtractsApi: String = servicesConfig.baseUrl("trade-reporting-extracts") +
    configuration.get[String]("microservice.services.trade-reporting-extracts.context")

  val cdsEnrolmentIdentifier: EnrolmentConfig = configuration.get[EnrolmentConfig]("enrolment-config")
  val internalAuthToken: String               = configuration.get[String]("internal-auth.token")

  val thirdPartySelfRemovalEventName: String = configuration.get[String]("auditing.third-party-self-removal-event-name")
  val thirdPartyRemovalEventName: String     = configuration.get[String]("auditing.third-party-removal-event-name")
  val thirdPartyAddedEventName: String       = configuration.get[String]("auditing.third-party-added-event-name")
  val thirdPartyUpdatedEventName: String        = configuration.get[String]("auditing.third-party-updated-event-name")

}
