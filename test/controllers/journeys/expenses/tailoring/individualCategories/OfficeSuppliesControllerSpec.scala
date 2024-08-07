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

package controllers.journeys.expenses.tailoring.individualCategories

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.AccountingType.Accrual
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.journeys.expenses.individualCategories.OfficeSupplies
import models.journeys.expenses.individualCategories.OfficeSupplies.YesDisallowable
import org.mockito.Mockito.when
import pages.TradeAccountingType
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories.OfficeSuppliesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.individualCategories.OfficeSuppliesView

import scala.concurrent.Future

class OfficeSuppliesControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[OfficeSupplies](
      "OfficeSuppliesController",
      OfficeSuppliesPage
    ) {

  override def onPageLoadCall: Call        = routes.OfficeSuppliesController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call          = routes.OfficeSuppliesController.onSubmit(taxYear, businessId, NormalMode)
  override def onwardRoute: Call           = routes.GoodsToSellOrUseController.onPageLoad(taxYear, businessId, NormalMode)
  override def validAnswer: OfficeSupplies = YesDisallowable

  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      ExpensesCategoriesPage.toString -> IndividualCategories.toString,
      TradeAccountingType.toString    -> Accrual.toString
    )
  )

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OfficeSuppliesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

}
