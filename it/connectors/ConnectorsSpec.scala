package connectors

import base.IntegrationBaseSpec
import cats.implicits.catsSyntaxEitherId
import helpers.{PagerDutyAware, WiremockSpec}
import models.errors.{HttpError, HttpErrorBody}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.http.Status._
import play.api.libs.json.JsObject
import utils.PagerDutyHelper.PagerDutyKeys._

class ConnectorsSpec extends WiremockSpec with IntegrationBaseSpec with TableDrivenPropertyChecks {
  private val baseUrl = appConfig.selfEmploymentBEBaseUrl
  private val url     = "/just-for-test"
  private val fullUrl = s"$baseUrl$url"

  val errorCases = Table(
    ("status", "expectedStatus", "expectedPagerDuty"),
    (BAD_REQUEST, BAD_REQUEST, FOURXX_RESPONSE_FROM_CONNECTOR),
    (NOT_FOUND, NOT_FOUND, FOURXX_RESPONSE_FROM_CONNECTOR),
    (INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_FROM_CONNECTOR),
    (SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_FROM_CONNECTOR),
    (GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, UNEXPECTED_RESPONSE_FROM_CONNECTOR)
  )

  "post" must {
    "notify pager duty on failure" in new PagerDutyAware {
      forAll(errorCases) { (status, expectedStatus, expectedPagerDuty) =>
        stubPost(url, status)
        val httpError = HttpError(expectedStatus, HttpErrorBody.parsingError)
        val result    = post(httpClient, fullUrl, mtditid, JsObject.empty).futureValue
        result shouldBe httpError.asLeft
        loggedErrors.exists(_.contains(expectedPagerDuty.toString)) shouldBe true
      }
    }
  }

  "get" must {
    "notify pager duty on failure" in new PagerDutyAware {
      forAll(errorCases) { (status, expectedStatus, expectedPagerDuty) =>
        stubGetWithoutResponseBody(url, status)
        val httpError = HttpError(expectedStatus, HttpErrorBody.parsingError)
        val result    = get[JsObject](httpClient, fullUrl, mtditid).futureValue
        result shouldBe httpError.asLeft
        loggedErrors.exists(_.contains(expectedPagerDuty.toString)) shouldBe true
      }
    }
  }

}
