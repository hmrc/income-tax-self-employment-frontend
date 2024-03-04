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

package models.journeys

import models.common._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import queries.Gettable

import scala.math.BigDecimal.RoundingMode

package object capitalallowances {

  def calculateFullCost(useOutsideSEPercentagePage: Gettable[Int],
                        totalCostOfVehiclePage: Gettable[BigDecimal],
                        request: DataRequest[AnyContent],
                        businessId: BusinessId): Option[BigDecimal] = {
    val maybeUseOutsideSEPercentage = request.getValue(useOutsideSEPercentagePage, businessId)
    val percentageUsedForSE         = 1 - (maybeUseOutsideSEPercentage.getOrElse(0) / 100.00)
    request
      .getValue(totalCostOfVehiclePage, businessId)
      .map(s => (s * percentageUsedForSE).setScale(0, RoundingMode.HALF_UP))
  }

}
