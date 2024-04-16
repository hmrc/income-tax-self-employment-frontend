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

package models.journeys.adjustments

import models.common.BusinessId
import models.requests.DataRequest
import pages.prepop.adjustments._
import play.api.libs.json.{Json, OFormat}

case class AdjustmentsPrepopAnswers(includedNonTaxableProfits: Option[BigDecimal],
                                    accountingAdjustment: Option[BigDecimal],
                                    averagingAdjustment: Option[BigDecimal],
                                    outstandingBusinessIncome: Option[BigDecimal],
                                    balancingChargeOther: Option[BigDecimal],
                                    goodsAndServicesOwnUse: Option[BigDecimal],
                                    transitionProfitAmount: Option[BigDecimal],
                                    transitionProfitAccelerationAmount: Option[BigDecimal])

object AdjustmentsPrepopAnswers {
  implicit val formats: OFormat[AdjustmentsPrepopAnswers] = Json.format[AdjustmentsPrepopAnswers]

  def getFromRequest(request: DataRequest[_], businessId: BusinessId): AdjustmentsPrepopAnswers = AdjustmentsPrepopAnswers(
    request.getValue(IncludedNonTaxableProfits, businessId),
    request.getValue(AccountingAdjustment, businessId),
    request.getValue(AveragingAdjustment, businessId),
    request.getValue(OutstandingBusinessIncome, businessId),
    request.getValue(BalancingChargeOther, businessId),
    request.getValue(GoodsAndServicesOwnUse, businessId),
    request.getValue(TransitionProfitAmount, businessId),
    request.getValue(TransitionProfitAccelerationAmount, businessId)
  )
}
