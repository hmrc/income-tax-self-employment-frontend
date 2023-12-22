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
import connectors.httpParser.GetBusinessesHttpParser.{GetBusinessesHttpReads, GetBusinessesResponse}
import connectors.httpParser.JourneyStateParser.JourneyStateHttpWrites
import models.common._
import models.domain.ApiResultT
import models.journeys.{Journey, JourneyNameAndStatus, JourneyStatusData, TaskList}
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig) {
  private def buildUrl(url: String) = s"${appConfig.selfEmploymentBEBaseUrl}/income-tax-self-employment/$url"

  /** Used only for the UI tests
    */
  def clearDatabase()(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url      = buildUrl("test-clear-all-data")
    val response = post(http, url, Mtditid("mtditid-test-only"), "")
    EitherT(response)
  }

  def getBusinesses(nino: String, mtditid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = buildUrl(s"individuals/business/details/$nino/list")
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getBusiness(nino: String, businessId: BusinessId, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = buildUrl(s"individuals/business/details/$nino/${businessId.value}")
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getJourneyState(businessId: BusinessId, journey: Journey, taxYear: TaxYear, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[JourneyNameAndStatus] = {
    val url      = buildUrl(s"completed-section/${businessId.value}/$journey/${taxYear.value}")
    val response = get[JourneyNameAndStatus](http, url, Mtditid(mtditid))
    EitherT(response)
  }

  def saveJourneyState(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url      = buildUrl(s"completed-section/${ctx.businessId.value}/${ctx.journey}/${ctx.taxYear.value}")
    val response = put(http, url, ctx.mtditid, JourneyStatusData(status))
    EitherT(response)
  }

  def getTaskList(nino: String, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[TaskList] = {
    val url      = buildUrl(s"${taxYear.value}/$nino/task-list")
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
