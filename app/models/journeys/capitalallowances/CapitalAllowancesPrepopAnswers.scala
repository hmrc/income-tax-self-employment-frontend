package models.journeys.capitalallowances

import models.common.BusinessId
import models.requests.DataRequest
import pages.prepop.CapitalAllowances._
import play.api.libs.json.{Json, OFormat}

case class CapitalAllowancesPrepopAnswers (annualInvestment: Option[BigDecimal],
                                           mainRatePool: Option[BigDecimal],
                                           specialRatePool: Option[BigDecimal],
                                           zeroEmissionCar: Option[BigDecimal],
                                           zeroEmissionGoodsVehicle: Option[BigDecimal],
                                           electricVehicleChargePoint: Option[BigDecimal],
                                           specialTaxSite: Option[BigDecimal], //TODO: Change to list of special tax sites
                                           structureAndBuildings: Option[BigDecimal], //TODO: Change to list of structure and buildings
                                           tradingAllowance: Option[BigDecimal],
                                           totalCapitalAllowances: Option[BigDecimal]
                                          )

object CapitalAllowancesPrepopAnswers {
  implicit val formats: OFormat[CapitalAllowancesPrepopAnswers] = Json.format[CapitalAllowancesPrepopAnswers]

  def getFromRequest(request: DataRequest[_], businessId: BusinessId): CapitalAllowancesPrepopAnswers = CapitalAllowancesPrepopAnswers(
    request.getValue(AnnualInvestment, businessId),
    request.getValue(MainRatePool, businessId),
    request.getValue(SpecialRatePool, businessId),
    request.getValue(ZeroEmissionCar, businessId),
    request.getValue(ZeroEmissionGoodsVehicle, businessId),
    request.getValue(ElectricVehicleChargePoint, businessId),
    request.getValue(SpecialTaxSite, businessId),
    request.getValue(StructureAndBuildings, businessId),
    request.getValue(TradingAllowance, businessId),
    request.getValue(TotalCapitalAllowances, businessId)
  )
}
