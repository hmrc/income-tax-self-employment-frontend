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

package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvOnlyForSelfEmploymentFormProvider
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvOnlyForSelfEmploymentPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvOnlyForSelfEmploymentView

class ZegvOnlyForSelfEmploymentControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec("ZegvOnlyForSelfEmploymentController", ZegvOnlyForSelfEmploymentPage) {

  def onPageLoadCall: Call = routes.ZegvOnlyForSelfEmploymentController.onPageLoad(taxYear, businessId, NormalMode)

  def onSubmitCall: Call = routes.ZegvOnlyForSelfEmploymentController.onSubmit(taxYear, businessId, NormalMode)

  def onwardRoute: Call = routes.ZegvHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode)

  def validAnswer: Boolean = true

  def createForm(userType: UserType): Form[Boolean] = new ZegvOnlyForSelfEmploymentFormProvider()(userType, taxYear)

  def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ZegvOnlyForSelfEmploymentView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, businessId.some).success.value

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns filledUserAnswers.asFuture
}
