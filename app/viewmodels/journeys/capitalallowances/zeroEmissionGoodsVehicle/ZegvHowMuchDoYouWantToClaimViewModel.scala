package viewmodels.journeys.capitalallowances.zeroEmissionGoodsVehicle

import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider.ZegvHowMuchDoYouWantToClaimModel
import models.common._
import models.journeys.capitalallowances.calculateFullCost
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim.{FullCost, LowerAmount}
import models.requests.DataRequest
import pages.capitalallowances.zeroEmissionGoodsVehicle.{
  ZegvClaimAmountPage,
  ZegvHowMuchDoYouWantToClaimPage,
  ZegvTotalCostOfVehiclePage,
  ZegvUseOutsideSEPercentagePage
}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent

object ZegvHowMuchDoYouWantToClaimViewModel {

  def createFillForm(request: DataRequest[AnyContent], businessId: BusinessId)(implicit
      messages: Messages): Option[(Form[ZegvHowMuchDoYouWantToClaimModel], BigDecimal)] = {
    val calculatedCost = calcFullCost(request, businessId)
    calculatedCost.map { fullCost =>
      val formProvider: Form[ZegvHowMuchDoYouWantToClaimModel] = ZegvHowMuchDoYouWantToClaimFormProvider(request.userType, fullCost)
      val howMuchDoYouWantToClaim                              = request.getValue(ZegvHowMuchDoYouWantToClaimPage, businessId)
      val totalCost                                            = request.getValue(ZegvClaimAmountPage, businessId)

      val filledForm = howMuchDoYouWantToClaim.map {
        case FullCost =>
          formProvider.fill(ZegvHowMuchDoYouWantToClaimModel(FullCost, None))
        case LowerAmount =>
          formProvider.fill(ZegvHowMuchDoYouWantToClaimModel(LowerAmount, totalCost))
      }

      (filledForm.getOrElse(formProvider), fullCost)
    }
  }

  def calcFullCost(request: DataRequest[AnyContent], businessId: BusinessId): Option[BigDecimal] =
    calculateFullCost(ZegvUseOutsideSEPercentagePage, ZegvTotalCostOfVehiclePage, request, businessId)

}
