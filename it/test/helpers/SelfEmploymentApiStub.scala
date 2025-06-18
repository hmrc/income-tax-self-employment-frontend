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

package test.helpers

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, stubFor, urlEqualTo}
import models.common.{BusinessId, Mtditid, Nino}
import play.api.libs.json.{JsValue, Json}

object SelfEmploymentApiStub {
  private def buildUrl(url: String) = s"/income-tax-self-employment/$url"

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(status: Int, response: JsValue = Json.obj()): Unit = {
    val path = buildUrl(s"individuals/business/details/${nino.value}/${businessId.value}")

    stubFor(
      WireMock
        .get(urlEqualTo(path))
        .withHeader("mtditid", equalTo(mtditid.value))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response.toString())
        )
    )
  }
}
