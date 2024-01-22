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

package controllers.journeys.capitalallowances.tailoring

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import forms.capitalallowances.tailoring.ClaimCapitalAllowancesFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.{BusinessId, Mtditid, Nino, UserType}
import models.database.UserAnswers
import navigation.{CapitalAllowancesNavigator, FakeCapitalAllowanceNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.tailoring.ClaimCapitalAllowancesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.tailoring.ClaimCapitalAllowancesView

class ClaimCapitalAllowancesControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec("ClaimCapitalAllowancesController", ClaimCapitalAllowancesPage) {

  private val accountingType = Accrual

  override lazy val onPageLoadCall: Call = routes.ClaimCapitalAllowancesController.onPageLoad(taxYear, businessId, NormalMode)
  override lazy val onSubmitCall: Call   = routes.ClaimCapitalAllowancesController.onSubmit(taxYear, businessId, NormalMode)

  override val onwardRoute: Call = models.common.onwardRoute

  override val validAnswer: Boolean = true

  override def createForm(user: UserType): Form[Boolean] = new ClaimCapitalAllowancesFormProvider()(user)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ClaimCapitalAllowancesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, accountingType, scenario.businessId).toString()
  }

  override val filledUserAnswers: UserAnswers = blankUserAnswers.set(page, validAnswer, businessId.some).success.value

  mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns accountingType.asRight.asFuture
  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns filledUserAnswers.asFuture

  override val bindings: List[Binding[_]] = List(
    bind[CapitalAllowancesNavigator].toInstance(new FakeCapitalAllowanceNavigator(onwardRoute))
  )
}