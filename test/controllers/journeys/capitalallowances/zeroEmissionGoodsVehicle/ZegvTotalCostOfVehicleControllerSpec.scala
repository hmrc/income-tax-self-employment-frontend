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

package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvTotalCostOfVehicleFormProvider
import models.NormalMode
import models.common.UserType
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvTotalCostOfVehiclePage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvTotalCostOfVehicleView

class ZegvTotalCostOfVehicleControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "ZegvTotalCostOfVehicleController",
      ZegvTotalCostOfVehiclePage
    ) {

  def onPageLoadRoute = routes.ZegvTotalCostOfVehicleController.onPageLoad(taxYear, businessId, NormalMode).url
  def onSubmitRoute   = routes.ZegvTotalCostOfVehicleController.onSubmit(taxYear, businessId, NormalMode).url
  def onwardRoute     = routes.ZegvOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

  override def createForm(userType: UserType): Form[BigDecimal] = new ZegvTotalCostOfVehicleFormProvider()()

  def expectedView(form: Form[_], scenario: TestScenario)(implicit request: Request[_], messages: Messages, application: Application): String = {
    val view = application.injector.instanceOf[ZegvTotalCostOfVehicleView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
