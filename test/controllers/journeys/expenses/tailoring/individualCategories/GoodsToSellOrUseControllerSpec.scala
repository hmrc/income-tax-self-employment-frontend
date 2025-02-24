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
import cats.data.EitherT
import models.common.AccountingType.Accrual
import models.common.Journey.ExpensesGoodsToSellOrUse
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.journeys.expenses.individualCategories.GoodsToSellOrUse
import models.journeys.expenses.individualCategories.OfficeSupplies.YesDisallowable
import models.requests.DataRequest
import models.{CheckMode, Mode, NormalMode}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.when
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories._
import pages.{OneQuestionPage, TradeAccountingType}
import play.api.Application
import play.api.data.{Form, FormBinding}
import play.api.http.Status.SEE_OTHER
import play.api.i18n.Messages
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Results.SeeOther
import play.api.mvc.{AnyContent, AnyContentAsFormUrlEncoded, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, await, redirectLocation, route, running, status, writeableOf_AnyContentAsFormUrlEncoded}
import views.html.journeys.expenses.tailoring.individualCategories.GoodsToSellOrUseView

import scala.concurrent.Future

class GoodsToSellOrUseControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[GoodsToSellOrUse](
      "GoodsToSellOrUseController",
      GoodsToSellOrUsePage
    ) {

  override def onPageLoadCall: Call          = routes.GoodsToSellOrUseController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call            = routes.GoodsToSellOrUseController.onSubmit(taxYear, businessId, NormalMode)
  override def onwardRoute: Call             = routes.RepairsAndMaintenanceController.onPageLoad(taxYear, businessId, NormalMode)
  override def validAnswer: GoodsToSellOrUse = GoodsToSellOrUse.YesDisallowable

  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      ExpensesCategoriesPage.toString -> IndividualCategories.toString,
      TradeAccountingType.toString    -> Accrual.toString,
      OfficeSuppliesPage.toString     -> YesDisallowable.toString
    )
  )

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[GoodsToSellOrUseView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

  forAll(userTypeCases) { userType =>
    s"$controllerName for $userType" - {

      "clearGoodsToSellOrUseExpensesData when in CheckMode" in new TestScenario(
        userType,
        Some(filledUserAnswers)
      ) {
        val newAnswer: GoodsToSellOrUse = GoodsToSellOrUse.No

        def onSubmitCheckModeCall: Call = routes.GoodsToSellOrUseController.onSubmit(taxYear, businessId, CheckMode)
        def postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, onSubmitCheckModeCall.url).withFormUrlEncodedBody(("value", newAnswer.toString))

        def expectedUrl: Call = controllers.journeys.expenses.tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)

        running(application) {
          mockService.handleForm(*[Form[_]], *, *)(*[DataRequest[_]], *[FormBinding]) returns SeeOther(onwardRoute.url).asFuture
          mockService.defaultHandleForm(*[Form[Any]], *[OneQuestionPage[Any]], *[BusinessId], *[TaxYear], *[Mode], *)(
            *[DataRequest[_]],
            *[FormBinding],
            *[Writes[Any]]
          ) returns SeeOther(onwardRoute.url).asFuture

          val userAnswers                               = buildUserAnswers(GoodsToSellOrUsePage, newAnswer)
          implicit val request: DataRequest[AnyContent] = fakeDataRequest(userAnswers)

          mockService
            .clearExpensesData(taxYear, businessId, ExpensesGoodsToSellOrUse)
            .returns(EitherT.rightT[Future, ServiceError](()))

          val result = route(application, postRequest).value

          val redirectMatchesExpectedUrl = expectedUrl.url.endsWith(redirectLocation(result).value)
          status(result) mustEqual SEE_OTHER
          await(result)
          assert(redirectMatchesExpectedUrl)
        }
      }
    }
  }

}
