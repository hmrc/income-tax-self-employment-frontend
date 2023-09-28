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

import config.FrontendAppConfig
import connectors.httpParser.GetBusinessesHttpParser.{GetBusinessesHttpReads, GetBusinessesResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentConnector @Inject()(http: HttpClient, appConfig: FrontendAppConfig) {

  def getBusinesses(nino: String, mtditid: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/individuals/business/details/$nino"
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

  def getBusiness(nino: String, businessId: String, mtditid: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessesResponse] = {

    val url = appConfig.selfEmploymentBEBaseUrl + s"/income-tax-self-employment/individuals/business/details/$nino/$businessId"
    http.GET[GetBusinessesResponse](url)(GetBusinessesHttpReads, hc.withExtraHeaders(headers = "mtditid" -> mtditid), ec)
  }

}
