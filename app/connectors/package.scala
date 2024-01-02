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

import models.common.Mtditid
import models.errors.ServiceError
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

package object connectors {
  type ContentResponse[A] = Either[ServiceError, A]
  type NoContentResponse  = ContentResponse[Unit]

  def post[A: Writes](http: HttpClient, url: String, mtditid: Mtditid, body: A)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceError, Unit]] =
    http.POST[A, Either[ServiceError, Unit]](url, body)(
      wts = implicitly[Writes[A]],
      rds = NoContentHttpReads,
      hc = addExtraHeaders(hc, mtditid),
      ec = ec
    )

  def put[A: Writes](http: HttpClient, url: String, mtditid: Mtditid, body: A)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceError, Unit]] =
    http.PUT[A, Either[ServiceError, Unit]](url, body)(
      wts = implicitly[Writes[A]],
      rds = NoContentHttpReads,
      hc = addExtraHeaders(hc, mtditid),
      ec = ec
    )

  def get[A: Reads](http: HttpClient, url: String, mtditid: Mtditid)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceError, A]] =
    http.GET(url)(
      rds = new ContentHttpReads[A],
      hc = addExtraHeaders(hc, mtditid),
      ec = ec
    )

  def getOpt[A: Reads](http: HttpClient, url: String, mtditid: Mtditid)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceError, Option[A]]] =
    http.GET(url)(
      rds = new OptionalContentHttpReads[A],
      hc = addExtraHeaders(hc, mtditid),
      ec = ec
    )

  def isSuccess(status: Int): Boolean = status >= 200 && status <= 299

  def isNoContent(status: Int): Boolean = status == 204

  private def addExtraHeaders(hc: HeaderCarrier, mtditid: Mtditid) =
    hc.withExtraHeaders(headers = "mtditid" -> mtditid.value)

}
