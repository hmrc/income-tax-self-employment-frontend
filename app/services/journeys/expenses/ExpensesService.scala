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

package services.journeys.expenses

import connectors.SelfEmploymentConnector
import models.errors.HttpError
import models.journeys.Journey
import models.journeys.expenses.ExpensesData
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExpensesService @Inject() (connector: SelfEmploymentConnector) {

  def sendExpensesAnswers[T](data: ExpensesData, answers: T, journey: Journey)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      writes: Writes[T]): Future[Either[HttpError, Unit]] =
    connector.sendExpensesAnswers(data, journey, answers)

}
