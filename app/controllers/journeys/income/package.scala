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

package controllers.journeys

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import models.common.BusinessId
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.ServiceError
import models.errors.ServiceError.NotFoundError
import pages.income.{HowMuchTradingAllowancePage, NonTurnoverIncomeAmountPage, TradingAllowanceAmountPage, TurnoverIncomeAmountPage}
import services.SelfEmploymentService

import scala.concurrent.{ExecutionContext, Future}

package object income {

  private val maxAllowance = BigDecimal(1000.00)

  def getMaxTradingAllowance(businessId: BusinessId, userAnswers: UserAnswers): Either[ServiceError, BigDecimal] =
    userAnswers
      .get(TurnoverIncomeAmountPage, businessId)
      .fold(NotFoundError(s"TurnoverIncomeAmountPage value not found for business ID: ${businessId.value}").asLeft[BigDecimal]) { turnover =>
        val totalAmount = turnover + userAnswers.get(NonTurnoverIncomeAmountPage, businessId).getOrElse(0)
        if (totalAmount > maxAllowance) maxAllowance.asRight else totalAmount.asRight
      }

  def returnOptionalTotalIncome(maybeTotalIncome: ApiResultT[BigDecimal])(implicit ec: ExecutionContext): ApiResultT[Option[BigDecimal]] = {
    val result = maybeTotalIncome.value.map {
      case Right(amount) => Some(amount).asRight
      case Left(_)       => None.asRight
    }
    EitherT[Future, ServiceError, Option[BigDecimal]](result)
  }

  def clearAllowancePagesData(answerUnchanged: Boolean, businessId: BusinessId, userAnswers: UserAnswers)(implicit
      ec: ExecutionContext): Future[UserAnswers] =
    if (answerUnchanged) Future(userAnswers)
    else
      Future.fromTry(
        SelfEmploymentService.clearDataFromUserAnswers(userAnswers, List(HowMuchTradingAllowancePage, TradingAllowanceAmountPage), Some(businessId)))
}
