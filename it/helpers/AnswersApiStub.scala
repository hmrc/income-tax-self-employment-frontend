package helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.common.JourneyContext
import play.api.libs.json.{JsValue, Json}

object AnswersApiStub {

  def answersApiUrl(ctx: JourneyContext) =
    s"/income-tax-self-employment/answers/users/${ctx.nino}/businesses/${ctx.businessId}/years/${ctx.taxYear.endYear}/journeys/${ctx.journey.entryName}"

  def collectionApiUrl(ctx: JourneyContext, index: Int): String = answersApiUrl(ctx) + s"/$index"

  def getAnswers(ctx: JourneyContext)(status: Int, response: Option[JsValue] = None): StubMapping =
    stubFor(
      get(urlMatching(answersApiUrl(ctx)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")
            .withBody(response.map(Json.stringify).getOrElse(""))
        )
    )

  def getIndex(ctx: JourneyContext, index: Int)(status: Int, response: Option[JsValue] = None): StubMapping =
    stubFor(
      get(urlMatching(collectionApiUrl(ctx, index)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")
            .withBody(response.map(Json.stringify).getOrElse(""))
        )
    )

  def replaceAnswers(ctx: JourneyContext, body: JsValue)(status: Int): StubMapping =
    stubFor(
      put(urlMatching(answersApiUrl(ctx)))
        .withRequestBody(equalToJson(Json.stringify(body)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")
            .withBody(Json.stringify(body))
        )
    )

  def replaceIndex(ctx: JourneyContext, body: JsValue, index: Int)(status: Int): StubMapping =
    stubFor(
      put(urlMatching(collectionApiUrl(ctx, index)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")
            .withBody(Json.stringify(body))
        )
    )

  def deleteAnswers(ctx: JourneyContext)(status: Int): StubMapping =
    stubFor(
      delete(urlMatching(answersApiUrl(ctx)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")
        )
    )

  def deleteIndex(ctx: JourneyContext, index: Int)(status: Int): StubMapping =
    stubFor(
      delete(urlMatching(collectionApiUrl(ctx, index)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader("Content-Type", "application/json; charset=utf-8")
        )
    )

}
