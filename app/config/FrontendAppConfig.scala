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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.common._
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[FrontendAppConfigImpl])
trait FrontendAppConfig {
  def host: String
  def appName: String
  def contactHost: String
  def contactFormServiceIdentifier: String
  def feedbackUrl(implicit request: RequestHeader): String
  def loginUrl: String
  def loginContinueUrl: String
  def signOutUrl: String
  def signInUrl: String
  def exitSurveyBaseUrl: String
  def exitSurveyUrl: String
  def languageTranslationEnabled: Boolean
  def languageMap: Map[String, Lang]
  def selfEmploymentBEBaseUrl: String
  def timeout: Int
  def countdown: Int
  def cacheTtl: Int
  def incomeTaxSubmissionBaseUrl: String
  def vcSessionServiceBaseUrl: String
  def incomeTaxSubmissionIvRedirect: String
  def incomeTaxSubmissionStartUrl(taxYear: Int): String
  def viewAndChangeEnterUtrUrl: String
  def viewAndChangeViewUrlAgent: String
  def travelExpensesShortJourneyEnabled: Boolean
  def sessionCookieServiceEnabled: Boolean

  // Answers API
  def answersApiUrl(ctx: JourneyContext): String
}

@Singleton
class FrontendAppConfigImpl @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) extends FrontendAppConfig {

  override val host: String    = configuration.get[String]("host")
  override val appName: String = configuration.get[String]("appName")

  override val contactHost                  = configuration.get[String]("contact-frontend.host")
  override val contactFormServiceIdentifier = "income-tax-self-employment-frontend"

  override def feedbackUrl(implicit request: RequestHeader): String =
    url"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}".toString

  override val loginUrl: String         = configuration.get[String]("urls.login")
  override val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  override val signOutUrl: String       = configuration.get[String]("urls.signOut")
  override val signInUrl: String        = s"$loginUrl?continue=${url"$loginContinueUrl"}&origin=$appName"

  override val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  override val exitSurveyUrl: String     = s"$exitSurveyBaseUrl/feedback/income-tax-self-employment-frontend"

  // Feature switching
  override val languageTranslationEnabled: Boolean        = configuration.get[Boolean]("feature-switch.welsh-translation")
  override val travelExpensesShortJourneyEnabled: Boolean = configuration.get[Boolean]("feature-switch.travel-expenses-short-journey")

  override val languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  override val selfEmploymentBEBaseUrl: String = servicesConfig.baseUrl("income-tax-self-employment")

  override val vcSessionServiceBaseUrl: String = servicesConfig.baseUrl("income-tax-session-data")

  override val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  override val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  override val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  override val incomeTaxSubmissionBaseUrl: String = configuration.get[String]("microservice.services.income-tax-submission-frontend.url") +
    configuration.get[String]("microservice.services.income-tax-submission-frontend.context")

  override val incomeTaxSubmissionIvRedirect: String = incomeTaxSubmissionBaseUrl +
    configuration.get[String]("microservice.services.income-tax-submission-frontend.iv-redirect")

  override def incomeTaxSubmissionStartUrl(taxYear: Int): String = s"$incomeTaxSubmissionBaseUrl/$taxYear/start"

  val vcBaseUrl = configuration.get[String]("microservice.services.view-and-change.url")

  override val viewAndChangeEnterUtrUrl: String  = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  override val viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents"

  def answersApiUrl(ctx: JourneyContext): String =
    s"$selfEmploymentBEBaseUrl/income-tax-self-employment/answers/users/${ctx.nino}/businesses/${ctx.businessId}" +
      s"/years/${ctx.taxYear}/journeys/${ctx.journey.entryName}"

  lazy val sessionCookieServiceEnabled: Boolean = servicesConfig.getBoolean("feature-switch.sessionCookieServiceEnabled")

}
