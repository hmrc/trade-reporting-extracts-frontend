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

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class FakeFrontendAppConfig
    extends FrontendAppConfig(
      configuration = Configuration(
        "host"                                                   -> "http://localhost:9000",
        "appName"                                                -> "trade-reporting-extracts-frontend",
        "contact-frontend.host"                                  -> "http://localhost:9250",
        "urls.login"                                             -> "http://localhost:9949/auth-login-stub/gg-sign-in",
        "urls.loginContinue"                                     -> "http://localhost:9000",
        "urls.signOut"                                           -> "http://localhost:9025/gg/sign-out",
        "urls.signOutContinue"                                   -> "http://localhost:9000",
        "urls.cdsSubscribeUrl"                                   -> "http://localhost:9000",
        "urls.guidanceWhatsInTheReportUrl"                       -> "http://localhost:9000",
        "urls.importsExportsContactUrl"                          -> "http://localhost:9000",
        "urls.manageEmailGuideUrl"                               -> "http://localhost:9000",
        "timeout-dialog.timeout"                                 -> 900,
        "timeout-dialog.countdown"                               -> 120,
        "mongodb.timeToLiveInSeconds"                            -> 1,
        "microservice.services.feedback-frontend.host"           -> "localhost",
        "microservice.services.feedback-frontend.port"           -> "9250",
        "microservice.services.feedback-frontend.protocol"       -> "http",
        "microservice.services.trade-reporting-extracts.baseUrl" -> "http://localhost:1234",
        "microservice.services.trade-reporting-extracts.context" -> "/trade-reporting-extracts",
        "enrolment-config.enrolment-key"                         -> "HMRC-CTS-ORG",
        "enrolment-config.enrolment-identifier"                  -> "EORINumber",
        "auditing.third-party-self-removal-event-name"           -> "third-party-self-removal",
        "auditing.third-party-removal-event-name"                -> "third-party-removal",
        "auditing.third-party-added-event-name"                  -> "third-party-added",
        "auditing.third-party-updated-event-name"                -> "third-party-updated",
        "additional-email-limit"                                 -> 5
      ),
      servicesConfig = new ServicesConfig(
        Configuration(
          "microservice.services.trade-reporting-extracts.baseUrl" -> "http://localhost:1234"
        )
      )
    ) {
  override val cacheTtl: Long = 1L
}
