/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  lazy val host: String    = configuration.get[String]("host")
  lazy val appName: String = configuration.get[String]("appName")

  private lazy val contactHost                  = configuration.get[String]("contact-frontend.host")
  private lazy val contactFormServiceIdentifier = "income-tax-self-employment-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    url"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}".toString

  def loginUrl: String         = configuration.get[String]("urls.login")
  def loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val signOutUrl: String  = configuration.get[String]("urls.signOut")

  private lazy val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  lazy val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/income-tax-self-employment-frontend"

  // Feature switching
  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("features.welsh-translation")
  def emaSupportingAgentsEnabled: Boolean      = configuration.get[Boolean]("features.ema-supporting-agents-enabled")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  lazy val selfEmploymentBEBaseUrl: String = servicesConfig.baseUrl("income-tax-self-employment")

  lazy val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  lazy val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  def incomeTaxSubmissionBaseUrl: String = configuration.get[String]("microservice.services.income-tax-submission.url") +
    configuration.get[String]("microservice.services.income-tax-submission-frontend.context")

  def incomeTaxSubmissionIvRedirect: String = incomeTaxSubmissionBaseUrl +
    configuration.get[String]("microservice.services.income-tax-submission-frontend.iv-redirect")

  def incomeTaxSubmissionStartUrl(taxYear: Int): String = s"$incomeTaxSubmissionBaseUrl/$taxYear/start"

  def viewAndChangeEnterUtrUrl: String = configuration.get[String]("microservice.services.view-and-change.url") +
    "/report-quarterly/income-and-expenses/view/agents/client-utr"
}
