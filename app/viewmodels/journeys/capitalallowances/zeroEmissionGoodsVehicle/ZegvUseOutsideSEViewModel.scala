package viewmodels.journeys.capitalallowances.zeroEmissionGoodsVehicle

import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEFormProvider
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEFormProvider.ZegvUseOutsideSEFormModel
import models.common.BusinessId
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSE._
import models.requests.DataRequest
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent

object ZegvUseOutsideSEViewModel {

  def createFilledForm(request: DataRequest[AnyContent], businessId: BusinessId)(implicit messages: Messages): Form[ZegvUseOutsideSEFormModel] = {
    val formProvider    = ZegvUseOutsideSEFormProvider(request.userType)
    val radioValue      = request.getValue(ZegvUseOutsideSEPage, businessId)
    val percentageValue = request.getValue(ZegvUseOutsideSEPercentagePage, businessId)

    val filledForm = radioValue.flatMap {
      case radio @ (Ten | TwentyFive | Fifty) =>
        Some(formProvider.fill(ZegvUseOutsideSEFormModel(radio)))
      case radio @ DifferentAmount =>
        percentageValue.map(percentage => formProvider.fill(ZegvUseOutsideSEFormModel(radio, percentage)))
    }

    filledForm.getOrElse(formProvider)
  }
}
