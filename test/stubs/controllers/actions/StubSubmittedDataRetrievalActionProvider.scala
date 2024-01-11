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

package stubs.controllers.actions

import base.SpecBase._
import cats.data.EitherT
import cats.implicits._
import controllers.actions.{SubmittedDataRetrievalAction, SubmittedDataRetrievalActionProvider}
import models.common.{JourneyContext, TaxYear}
import models.domain.ApiResultT
import models.errors.ServiceError
import models.journeys.{TaskList, TaskListWithRequest}
import models.requests.OptionalDataRequest
import play.api.libs.json.Format
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubSubmittedDataRetrievalActionProvider(
    loadTaskListRes: Either[ServiceError, TaskListWithRequest] = TaskListWithRequest(TaskList.empty, fakeOptionalRequest).asRight
) extends SubmittedDataRetrievalActionProvider {

  def apply[SubsetOfAnswers: Format](mkJourneyContext: OptionalDataRequest[_] => JourneyContext)(implicit
      ec: ExecutionContext): SubmittedDataRetrievalAction =
    StubSubmittedDataRetrievalAction()

  def loadTaskList(taxYear: TaxYear, request: OptionalDataRequest[AnyContent])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[TaskListWithRequest] =
    EitherT.fromEither[Future](loadTaskListRes)
}
