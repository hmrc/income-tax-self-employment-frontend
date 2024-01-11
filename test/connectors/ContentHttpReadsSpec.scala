/*
 * Copyright 2024 HM Revenue & Customs
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

import models.errors.ServiceError.{CannotParseJsonError, CannotReadJsonError}
import models.journeys.income.IncomeJourneyAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Reads._
import uk.gov.hmrc.http.HttpResponse
import org.scalatest.EitherValues._

class ContentHttpReadsSpec extends AnyWordSpecLike with Matchers {

  "read" should {
    val httpReads = new ContentHttpReads[JsObject]
    val okResponse = HttpResponse(
      200,
      _: String,
      Map.empty
    )

    "parse json" in {
      val json   = """{"foo": "bar"}"""
      val result = httpReads.read("GET", "url", okResponse(json))

      result shouldBe Right(Json.parse(json))
    }

    "return an error if invalid json" in {
      val json   = """{"foo": "bar"""
      val result = httpReads.read("GET", "url", okResponse(json))

      result.left.value shouldBe a[CannotParseJsonError]
    }

    "return an error if cannot be deserialized to a case class" in {
      val httpReads = new ContentHttpReads[IncomeJourneyAnswers]
      val json      = """{"foo": "bar"}"""
      val result    = httpReads.read("GET", "url", okResponse(json))

      result.left.value shouldBe a[CannotReadJsonError]
    }

  }
}
