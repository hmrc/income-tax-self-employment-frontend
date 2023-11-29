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

package helpers

import base.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsObject, Json, Writes}

trait WiremockStubHelpers {

  def stubGetWithResponseBody(url: String, status: Int, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): Unit = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(get(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(
      mappingWithHeaders
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
            .withHeader("Content-Type", "application/json; charset=utf-8")))

    ()
  }

  def stubGetWithoutResponseBody(url: String, status: Int): Unit = {
    stubFor(
      get(urlMatching(url))
        .willReturn(aResponse()
          .withStatus(status)))
    ()
  }

  def stubPostWithoutResponseAndRequestBody(url: String, status: Int): Unit = {
    stubFor(
      post(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")))
    ()
  }

  // TODO Reduce duplication here
  def stubPostWithRequestBody[T](url: String, requestBody: T, expectedStatus: Int, requestHeaders: Seq[HttpHeader] = Seq.empty)(implicit
      writes: Writes[T]): StubMapping = {

    val stringBody = writes.writes(requestBody).toString()

    val mapping: MappingBuilder = requestHeaders
      .foldLeft(post(urlMatching(url))) { (result, nxt) =>
        result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
      }
      .withRequestBody(equalTo(stringBody))

    stubFor(
      mapping
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withHeader("Content-Type", "application/json; charset=utf-8")))
  }

  def stubPutWithResponseBody(url: String, expectedStatus: Int, expectedResponse: String, requestHeaders: Seq[HttpHeader] = Seq.empty): Unit = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(put(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(
      mappingWithHeaders
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedResponse)
            .withHeader("Content-Type", "application/json; charset=utf-8")))
    ()
  }

  def stubPutWithoutResponseBody(url: String, status: Int): Unit = {
    stubFor(
      put(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")))
    ()
  }

  def auditStubs(): Unit = {
    val auditResponseCode = 204
    stubPostWithoutResponseAndRequestBody("/write/audit", auditResponseCode)
    stubPostWithoutResponseAndRequestBody("/write/audit/merged", auditResponseCode)
  }

  def stubPost(url: String, status: Int) =
    stubPostWithRequestBody(
      url = url,
      requestBody = JsObject.empty,
      expectedStatus = status,
      requestHeaders = List(new HttpHeader("mtditid", IntegrationBaseSpec.mtditid.value))
    )
}
