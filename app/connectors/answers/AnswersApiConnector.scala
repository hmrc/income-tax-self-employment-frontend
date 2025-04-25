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

package connectors.answers

import config.FrontendAppConfig
import connectors.answers.AnswersApiConnector.{invalidResponseMessage, parseValuesAsList, unexpectedErrorMessage, valuesKey}
import jakarta.inject.{Inject, Singleton}
import models.Index
import models.common._
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, InternalServerException, StringContextOps}
import utils.Logging

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnswersApiConnector @Inject() (httpClientV2: HttpClientV2, appConfig: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {

  def getAnswers[T](ctx: JourneyContext, index: Option[Index] = None)(implicit format: Format[T], hc: HeaderCarrier): Future[Option[T]] = {
    implicit object GetSectionHttpReads extends HttpReads[Option[T]] {
      override def read(method: String, url: String, response: HttpResponse): Option[T] =
        response.status match {
          case OK        => response.json.asOpt[T]
          case NOT_FOUND => None
          case _         => logAndThrow(unexpectedErrorMessage(ctx, index))
        }
    }

    httpClientV2
      .get(buildUrl(ctx, index))
      .setHeader("mtditid" -> ctx.mtditid.value)
      .execute
  }

  def getAnswersAsList[T](ctx: JourneyContext)(implicit format: Format[T], hc: HeaderCarrier): Future[List[T]] = {
    implicit object GetSectionsHttpReads extends HttpReads[List[T]] {
      override def read(method: String, url: String, response: HttpResponse): List[T] =
        response.status match {
          case OK        => parseValuesAsList(response.json)
          case NOT_FOUND => Nil
          case _         => logAndThrow(unexpectedErrorMessage(ctx))
        }
    }

    httpClientV2
      .get(buildUrl(ctx))
      .setHeader("mtditid" -> ctx.mtditid.value)
      .execute
  }

  def replaceAnswers[T](ctx: JourneyContext, data: T, index: Option[Index] = None)(implicit format: Format[T], hc: HeaderCarrier): Future[T] = {
    implicit object ReplaceSectionHttpReads extends HttpReads[T] {
      override def read(method: String, url: String, response: HttpResponse): T =
        response.status match {
          case OK =>
            response.json.asOpt[T] match {
              case Some(validatedModel) => validatedModel
              case None                 => logAndThrow(invalidResponseMessage(ctx, index))
            }
          case _ => logAndThrow(unexpectedErrorMessage(ctx, index))
        }
    }

    httpClientV2
      .put(buildUrl(ctx, index))
      .setHeader("mtditid" -> ctx.mtditid.value)
      .withBody(Json.toJson(data))
      .execute
  }

  def replaceAnswersAsList[T](ctx: JourneyContext, data: List[T])(implicit format: Format[T], hc: HeaderCarrier): Future[List[T]] = {
    implicit object ReplaceSectionHttpReads extends HttpReads[List[T]] {
      override def read(method: String, url: String, response: HttpResponse): List[T] =
        response.status match {
          case OK => parseValuesAsList(response.json)
          case _  => logAndThrow(unexpectedErrorMessage(ctx))
        }
    }

    httpClientV2
      .put(buildUrl(ctx))
      .setHeader("mtditid" -> ctx.mtditid.value)
      .withBody(Json.toJson(Map(valuesKey -> data)))
      .execute
  }

  def deleteAnswers(ctx: JourneyContext, index: Option[Index] = None)(implicit hc: HeaderCarrier): Future[Boolean] = {
    implicit object DeleteSectionHttpReads extends HttpReads[Boolean] {
      override def read(method: String, url: String, response: HttpResponse): Boolean =
        response.status match {
          case NO_CONTENT => true
          case _          => logAndThrow(unexpectedErrorMessage(ctx, index))
        }
    }

    httpClientV2
      .delete(buildUrl(ctx, index))
      .setHeader("mtditid" -> ctx.mtditid.value)
      .execute
  }

  private def buildUrl(ctx: JourneyContext, index: Option[Index] = None): URL = {
    val url = index.map(idx => s"${appConfig.answersApiUrl(ctx)}/${idx.value}").getOrElse(s"${appConfig.answersApiUrl(ctx)}")
    url"$url"
  }

  private def logAndThrow(message: String): Nothing = {
    logger.error(message)
    throw new InternalServerException(message)
  }

}

object AnswersApiConnector {
  private val valuesKey: String = "values"

  private def unexpectedErrorMessage(ctx: JourneyContext, index: Option[Index] = None): String =
    s"Unexpected error from answers API ${messageSuffix(ctx, index)}"

  private def invalidResponseMessage(ctx: JourneyContext, index: Option[Index] = None): String =
    s"Failed to parse answers ${messageSuffix(ctx, index)}"

  private def messageSuffix(ctx: JourneyContext, index: Option[Index]) = {
    val indexPrefix = index.map(idx => s"for index '$idx").getOrElse("")
    s"$indexPrefix for journey ${ctx.journey.entryName} for business '${ctx.businessId}' for tax year '${ctx.taxYear}'"
  }

  private def parseValuesAsList[T](json: JsValue)(implicit format: Format[T]): List[T] =
    (json \ valuesKey).validate[List[T]].asOpt match {
      case Some(validatedModel) => validatedModel
      case None                 => Nil
    }

}
