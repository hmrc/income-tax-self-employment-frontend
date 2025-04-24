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

package services.answers

import connectors.answers.AnswersApiConnector
import jakarta.inject.{Inject, Singleton}
import models.Index
import models.common.JourneyContext
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnswersService @Inject() (connector: AnswersApiConnector)(implicit ex: ExecutionContext) {

  def getAnswers[T](ctx: JourneyContext, index: Option[Index] = None)(implicit format: Format[T], hc: HeaderCarrier): Future[Option[T]] =
    connector.getAnswers(ctx, index)

  def getAnswersAsList[T](ctx: JourneyContext)(implicit format: Format[T], hc: HeaderCarrier): Future[List[T]] =
    connector.getAnswersAsList(ctx)

  def replaceAnswers[T](ctx: JourneyContext, data: T, index: Option[Index] = None)(implicit format: Format[T], hc: HeaderCarrier): Future[T] =
    connector.replaceAnswers(ctx, data, index)

  def replaceAnswersAsList[T](ctx: JourneyContext, data: List[T])(implicit format: Format[T], hc: HeaderCarrier): Future[List[T]] =
    connector.replaceAnswersAsList(ctx, data)

  def deleteAnswers(ctx: JourneyContext)(implicit hc: HeaderCarrier): Future[Boolean] =
    connector.deleteAnswers(ctx)
}
