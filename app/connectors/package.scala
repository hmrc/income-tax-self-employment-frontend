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

import connectors.NoContentHttpParser.NoContentHttpReads
import models.common.Mtditid
import models.errors.ServiceError
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{Json, Reads, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

package object connectors {
  type ContentResponse[A] = Either[ServiceError, A]
  type NoContentResponse  = ContentResponse[Unit]

  def post[A: Writes](http: HttpClientV2, url: String, mtditid: Mtditid, body: A)
                     (implicit hc: HeaderCarrier,
                      ec: ExecutionContext): Future[Either[ServiceError, Unit]] = {
    http.post(url"$url")(addExtraHeaders(hc, mtditid))
      .withBody(Json.toJson(body)(implicitly[Writes[A]]))
      .execute[NoContentResponse]
  }

  def put[A: Writes](http: HttpClientV2, url: String, mtditid: Mtditid, body: A)
                    (implicit hc: HeaderCarrier,
                     ec: ExecutionContext): Future[Either[ServiceError, Unit]] = {
    http.put(url"$url")(addExtraHeaders(hc, mtditid))
      .withBody(Json.toJson(body)(implicitly[Writes[A]]))
      .execute[NoContentResponse]
  }

  def get[A: Reads](http: HttpClientV2, url: String, mtditid: Mtditid)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceError, A]] = {
    implicit val contentHttpContentReads: HttpReads[ContentResponse[A]] =
      new ContentHttpReads[A]
    http.get(url"$url")(addExtraHeaders(hc, mtditid))
      .execute[ContentResponse[A]]
  }

  def getOpt[A: Reads](http: HttpClientV2, url: String, mtditid: Mtditid, allowUnprocessableEntity: Boolean = false)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceError, Option[A]]] = {
    implicit val optionalContentHttpContentReads: HttpReads[ContentResponse[Option[A]]] =
      new OptionalContentHttpReads[A](allowUnprocessableEntity)
    http.get(url"$url")(addExtraHeaders(hc, mtditid))
      .execute[ContentResponse[Option[A]]]
  }

  def isSuccess(status: Int): Boolean = status >= 200 && status <= 299

  def isNoContent(status: Int): Boolean = status == NO_CONTENT

  def isNotFound(status: Int): Boolean = status == NOT_FOUND

  def isUnprocessableEntity(allowUnprocessableEntity: Boolean, response: HttpResponse): Boolean =
    allowUnprocessableEntity && response.status == UNPROCESSABLE_ENTITY &&
      response.body.contains("INCOME_SUBMISSIONS_NOT_EXIST")

  private def addExtraHeaders(hc: HeaderCarrier, mtditid: Mtditid) =
    hc.withExtraHeaders(headers = "mtditid" -> mtditid.value)

}
