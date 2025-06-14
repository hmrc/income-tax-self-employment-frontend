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

package connectors

import config.FrontendAppConfig
import models.errors.ServiceError
import models.session.SessionData
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionDataConnector @Inject() (config: FrontendAppConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext) {

  implicit val sessionDataReads: OptionalContentHttpReads[SessionData] = new OptionalContentHttpReads[SessionData]

  def getSessionData(implicit hc: HeaderCarrier): Future[Either[ServiceError, Option[SessionData]]] =
    httpClient
      .get(url"${config.vcSessionServiceBaseUrl}/income-tax-session-data")
      .execute[Either[ServiceError, Option[SessionData]]]

}
