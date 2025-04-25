package helpers

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
