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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import connectors.httpParser.HttpParser.StringWrites
import models.common._
import models.domain.{ApiResultT, BusinessData}
import models.journeys.{Journey, JourneyNameAndStatus, JourneyStatusData, TaskList}
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SelfEmploymentConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig) {
  private def buildUrl(url: String) = s"${appConfig.selfEmploymentBEBaseUrl}/income-tax-self-employment/$url"

  /** Used only for the UI tests
    */
  def clearDatabase()(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url      = buildUrl("test-only/test-clear-all-data")
    val response = post(http, url, Mtditid("mtditid-test-only"), "")
    EitherT(response)
  }

  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Seq[BusinessData]] = {
    val url      = buildUrl(s"individuals/business/details/${nino.value}/list")
    val response = get[Seq[BusinessData]](http, url, mtditid)
    EitherT(response)
  }

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Seq[BusinessData]] = {
    val url      = buildUrl(s"individuals/business/details/${nino.value}/${businessId.value}")
    val response = get[Seq[BusinessData]](http, url, mtditid)
    EitherT(response)
  }

  def getJourneyState(businessId: BusinessId, journey: Journey, taxYear: TaxYear, mtditid: Mtditid)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[JourneyNameAndStatus] = {
    val url      = buildUrl(s"completed-section/${businessId.value}/$journey/${taxYear.endYear}")
    val response = get[JourneyNameAndStatus](http, url, mtditid)
    EitherT(response)
  }

  def saveJourneyState(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url      = buildUrl(s"completed-section/${ctx.businessId.value}/${ctx.journey}/${ctx.taxYear.endYear}")
    val response = put(http, url, ctx.mtditid, JourneyStatusData(status))
    EitherT(response)
  }

  def getTaskList(nino: Nino, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[TaskList] = {
    val url      = buildUrl(s"${taxYear.endYear}/${nino.value}/task-list")
    val response = get[TaskList](http, url, mtditid)
    EitherT(response)
  }

  def getSubmittedAnswers[A: Reads](context: JourneyContext)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[A]] = {
    val url      = buildUrl(context.answersUrl)
    val response = getOpt[A](http, url, context.mtditid)
    EitherT(response)
  }

  def submitAnswers[A: Writes](context: JourneyContext, answers: A)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url      = buildUrl(context.answersUrl)
    val response = post(http, url, context.mtditid, answers)
    EitherT(response)
  }
}
