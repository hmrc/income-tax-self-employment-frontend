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

package controllers.actions

import cats.data.EitherT
import connectors.SelfEmploymentConnector
import models.common.JourneyContext
import models.common.TaxYear
import models.domain.ApiResultT
import models.errors.ServiceError
import models.journeys.Journey
import models.journeys.TaskListWithRequest
import models.journeys.expenses.ExpensesTailoringAnswers
import models.journeys.income.IncomeJourneyAnswers
import models.requests.OptionalDataRequest
import models.requests.TradesJourneyStatuses
import play.api.libs.json.Format
import play.api.mvc.AnyContent
import repositories.SessionRepositoryBase
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait SubmittedDataRetrievalActionProvider {
  def apply[SubsetOfAnswers: Format](mkJourneyContext: OptionalDataRequest[_] => JourneyContext)(implicit
      ec: ExecutionContext): SubmittedDataRetrievalAction

  def loadTaskList(taxYear: TaxYear, request: OptionalDataRequest[AnyContent])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[TaskListWithRequest]
}

@Singleton
class SubmittedDataRetrievalActionProviderImpl @Inject() (connector: SelfEmploymentConnector, sessionRepository: SessionRepositoryBase)
    extends SubmittedDataRetrievalActionProvider {

  def apply[SubsetOfAnswers: Format](mkJourneyContext: OptionalDataRequest[_] => JourneyContext)(implicit
      ec: ExecutionContext): SubmittedDataRetrievalAction =
    new SubmittedDataRetrievalActionImpl[SubsetOfAnswers](mkJourneyContext, connector, sessionRepository)

  def loadTaskList(taxYear: TaxYear, request: OptionalDataRequest[AnyContent])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[TaskListWithRequest] = {
    val nino    = request.nino
    val mtditid = request.mtditid

    for {
      taskList <- connector.getTaskList(nino.value, taxYear, mtditid)
      businesses = taskList.businesses
      incomeUpdated   <- loadAnswers[IncomeJourneyAnswers](taxYear, businesses, request, Journey.Income)
      expensesUpdated <- loadAnswers[ExpensesTailoringAnswers](taxYear, businesses, incomeUpdated, Journey.ExpensesTailoring)
    } yield TaskListWithRequest(taskList, expensesUpdated)
  }

  private def loadAnswers[A: Format](taxYear: TaxYear,
                                     businesses: List[TradesJourneyStatuses],
                                     request: OptionalDataRequest[AnyContent],
                                     journey: Journey)(implicit ec: ExecutionContext) = {
    val result = businesses.foldLeft(Future.successful(request)) { (futureRequest, business) =>
      futureRequest.flatMap { updatedRequest =>
        apply[A](req => req.mkJourneyNinoContext(taxYear, business.businessId, journey))
          .execute(updatedRequest)
      }
    }

    EitherT.right[ServiceError](result)
  }
}
