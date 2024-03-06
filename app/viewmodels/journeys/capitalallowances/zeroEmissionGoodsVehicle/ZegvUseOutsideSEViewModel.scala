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
