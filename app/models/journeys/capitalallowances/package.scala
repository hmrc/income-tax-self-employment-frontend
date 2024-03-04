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
