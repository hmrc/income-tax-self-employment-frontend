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
import connectors.httpParser.GetTradesStatusHttpParser.{GetTradesStatusHttpReads, GetTradesStatusResponse}
import connectors.httpParser.JourneyStateParser.{JourneyStateHttpReads, JourneyStateHttpWrites, JourneyStateResponse}
import connectors.httpParser.SendJourneyAnswersHttpParser.{SendJourneyAnswersHttpReads, SendJourneyAnswersResponse}
import models.common.{BusinessId, SubmissionContext, TaxYear}
import connectors.httpParser.JourneyStateParser.{JourneyStateHttpReads, JourneyStateHttpWrites, JourneyStateResponse, pagerDutyError}
import connectors.httpParser.JourneyStateParser.{JourneyStateHttpReads, JourneyStateHttpWrites, JourneyStateResponse}
import connectors.httpParser.SendExpensesAnswersHttpParser.{SendExpensesAnswersHttpReads, SendExpensesAnswersResponse}
import models.common.{BusinessId, Mtditid, TaxYear}
import models.domain.ApiResultT
import models.errors.HttpError
import models.journeys.Journey
import models.journeys.expenses.ExpensesData
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig) {
  private def buildUrl(url: String) = s"${appConfig.selfEmploymentBEBaseUrl}$url"

  def getBusinesses(nino: String, mtditid: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/individuals/business/details/$nino/list")
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getBusiness(nino: String, businessId: BusinessId, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/individuals/business/details/$nino/${businessId.value}")
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getJourneyState(businessId: BusinessId, journey: String, taxYear: TaxYear, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[JourneyStateResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/completed-section/${businessId.value}/$journey/${taxYear.value}")
    http.GET[JourneyStateResponse](url)(JourneyStateHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def saveJourneyState(businessId: BusinessId, journey: String, taxYear: TaxYear, complete: Boolean, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[JourneyStateResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/completed-section/${businessId.value}/$journey/${taxYear.value}/$complete")

    http.PUT[String, JourneyStateResponse](url, "")(
      JourneyStateHttpWrites,
      JourneyStateHttpReads,
      hc.withExtraHeaders(headers = "mtditid" -> mtditid),
      ec)
  }

  def getCompletedTradesWithStatuses(nino: String, taxYear: TaxYear, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[GetTradesStatusResponse] = {

    val url = buildUrl(s"/income-tax-self-employment/individuals/business/journey-states/$nino/${taxYear.value}")
    http.GET[GetTradesStatusResponse](url)(GetTradesStatusHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  // TODO Use submitAnswers
  def sendJourneyAnswers[T](context: SubmissionContext, answers: T)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      writes: Writes[T]): Future[SendJourneyAnswersResponse] = {

    import context._

    val url = buildUrl(s"/income-tax-self-employment/send-journey-answers/${journey.toString}/${taxYear.value}/${businessId.value}/${nino.value}")

    http.POST[T, SendJourneyAnswersResponse](url, answers)(
      wts = writes,
      rds = SendJourneyAnswersHttpReads,
      hc = hc.withExtraHeaders(headers = "mtditid" -> mtditid.value),
      ec = ec
    )
  }

  def submitAnswers[A: Writes](taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, journey: Journey, answers: A)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] = {
    val url = buildUrl(s"/income-tax-self-employment/${taxYear}/${businessId.value}/${journey.toString}")
    val response = http.POST[A, Either[HttpError, Unit]](url, answers)(
      wts = implicitly[Writes[A]],
      rds = NoContentHttpReads,
      hc = hc.withExtraHeaders(headers = "mtditid" -> mtditid.value),
      ec = ec
    )
    EitherT(response)
  }
}
