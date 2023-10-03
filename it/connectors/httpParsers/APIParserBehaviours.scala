///*
// * Copyright 2023 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package connectors.httpParsers
//
//import base.SpecBase
//import connectors.httpParser.APIParser
//import models.errors.APIErrorBody.{APIError, APIErrors, APIStatusError}
//import play.api.http.Status.INTERNAL_SERVER_ERROR
//import play.api.libs.json.{JsValue, Json}
//import uk.gov.hmrc.http.HttpResponse
//
//trait APIParserBehaviours extends SpecBase{
//
//  def apiTypeName: String
//
//  object FakeParser extends APIParser {
//    val parserName: String = "TestParser"
//    val apiType: String = apiTypeName
//  }
//
//  val serviceUnavailableReason = "Dependent systems are currently not responding."
//  val serverErrorReason = "IFS is currently experiencing problems that require live service intervention."
//  val parsingErrorReason = "Error parsing response from API"
//
//  val svcUnavailJs = s"""{"code":"SERVICE_UNAVAILABLE", "reason":"$serviceUnavailableReason", "errorType":"DOWNSTREAM_ERROR_CODE"}"""
//  val svrErrJs = s"""{"code":"SERVER_ERROR", "reason":"$serverErrorReason", "errorType":"DOWNSTREAM_ERROR_CODE"}"""
//  val multiErrJs: String = s"""{"failures":[$svcUnavailJs, $svrErrJs]}""".stripMargin
//
//  def failureHttpResponse(json: JsValue): HttpResponse =
//    HttpResponse(INTERNAL_SERVER_ERROR, json, Map("CorrelationId" -> Seq("1234645654645")))
//
//  def parserShould(): Unit =
//    "FakeParser" mustBe {
//      logHttpResponse()
//      handleSingleError()
//      handleMultpleError()
//      returnParsingErrors()
//    }
//
//  def returnParsingErrors(): Unit =
//    "return a parsing error" mustBe {
//      returnJsonValidationError()
//      handleNonApiErrorResponseError()
//      handleNonJsonResponseBodyError()
//    }
//
//  def logHttpResponse(): Unit =
//    "log the correct message" in {
//      val result = FakeParser.logMessage(failureHttpResponse(Json.parse(multiErrJs)))
//      result mustBe
//        s"""[TestParser][read] Received 500 from $apiTypeName API. Body:{
//           |  "failures" : [ {
//           |    "code" : "SERVICE_UNAVAILABLE",
//           |    "reason" : "$serviceUnavailableReason",
//           |    "errorType" : "DOWNSTREAM_ERROR_CODE"
//           |  }, {
//           |    "code" : "SERVER_ERROR",
//           |    "reason" : "$serverErrorReason",
//           |    "errorType" : "DOWNSTREAM_ERROR_CODE"
//           |  } ]
//           |}  CorrelationId: 1234645654645""".stripMargin
//    }
//
//  def handleSingleError(): Unit =
//    "handle a single error" in {
//      val result = FakeParser.handleAPIError(failureHttpResponse(Json.parse(svrErrJs)))
//      result mustBe Left(APIStatusError(INTERNAL_SERVER_ERROR, APIError("SERVER_ERROR", serverErrorReason)))
//    }
//
//  def handleMultpleError(): Unit =
//    "handle a multiple error" in {
//      val result = FakeParser.handleAPIError(failureHttpResponse(Json.parse(multiErrJs)))
//      result mustBe Left(APIStatusError(INTERNAL_SERVER_ERROR, APIErrors(Seq(
//        APIError("SERVICE_UNAVAILABLE", serviceUnavailableReason),
//        APIError("SERVER_ERROR", serverErrorReason)
//      ))))
//    }
//
//  def returnJsonValidationError(): Unit =
//    "return a non model validating json error" in {
//      val result = FakeParser.nonModelValidatingJsonFromAPI
//      result mustBe Left(APIStatusError(INTERNAL_SERVER_ERROR, APIError("PARSING_ERROR", parsingErrorReason)))
//    }
//
//  def handleNonApiErrorResponseError(): Unit =
//    "handling a response that is neither a single or a multiple error" in {
//      val result = FakeParser.handleAPIError(failureHttpResponse(Json.obj()))
//      result mustBe Left(APIStatusError(INTERNAL_SERVER_ERROR, APIError("PARSING_ERROR", parsingErrorReason)))
//    }
//
//  def handleNonJsonResponseBodyError(): Unit =
//    "handling a response where the response body is not json" in {
//      val result = FakeParser.handleAPIError(HttpResponse(INTERNAL_SERVER_ERROR, "", Map("CorrelationId" -> Seq("1234645654645"))))
//      result mustBe Left(APIStatusError(INTERNAL_SERVER_ERROR, APIError("PARSING_ERROR", parsingErrorReason)))
//    }
//
//}
