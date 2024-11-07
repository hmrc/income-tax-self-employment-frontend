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

package controllers.journeys.income

import cats.data.EitherT
import config.TaxYearConfig.totalIncomeIsEqualOrAboveThreshold
import controllers.actions._
import controllers.{handleResultT, journeys}
import models.common.Journey.Income
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.domain.ApiResultT
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.requests.DataRequest
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.income.{NonTurnoverIncomeAmountPage, TurnoverIncomeAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.income.IncomeExpensesWarningView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeExpensesWarningController @Inject() (override val messagesApi: MessagesApi,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 service: SelfEmploymentService,
                                                 view: IncomeExpensesWarningView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getData andThen requireData)(implicit request => Ok(view(request.userType, taxYear, businessId)))

  private def clearSimplifiedExpensesIfTurnoverChangedToOverThreshold(
      previousIncomeWasUnderThreshold: Boolean,
      currentIncomeIsOverThreshold: Boolean,
      expensesTailoringIsSimplified: Boolean,
      ctx: JourneyContextWithNino)(implicit request: DataRequest[_], hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    if (previousIncomeWasUnderThreshold && currentIncomeIsOverThreshold && expensesTailoringIsSimplified) {
      service.clearSimplifiedExpensesData(ctx)
    } else {
      EitherT.rightT(())
    }

  private def maybeClearSimplifiedExpensesTailoringAnswers(
      ctx: JourneyContextWithNino)(implicit request: DataRequest[_], hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    for {
      maybeExistingTotalIncomeAmount <- returnOptionalTotalIncome(service.getTotalIncome(ctx))
      currentAnswersTurnoverAmount   <- EitherT.fromEither[Future](request.valueOrNotFoundError(TurnoverIncomeAmountPage, ctx.businessId))
      currentAnswersNonTurnoverAmount      = request.getValue[BigDecimal](NonTurnoverIncomeAmountPage, ctx.businessId).getOrElse(BigDecimal(0))
      currentAnswersTotalIncomeAmount      = currentAnswersTurnoverAmount + currentAnswersNonTurnoverAmount
      existingIncomeIsBelowThreshold       = maybeExistingTotalIncomeAmount.exists(!totalIncomeIsEqualOrAboveThreshold(_))
      currentIncomeIsEqualOrAboveThreshold = totalIncomeIsEqualOrAboveThreshold(currentAnswersTotalIncomeAmount)
      expensesTailoringIsSimplified        = request.getValue(ExpensesCategoriesPage, ctx.businessId).exists(_ != IndividualCategories)
      _ <- clearSimplifiedExpensesIfTurnoverChangedToOverThreshold(
        existingIncomeIsBelowThreshold,
        currentIncomeIsEqualOrAboveThreshold,
        expensesTailoringIsSimplified,
        ctx)
    } yield ()

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, Income)

      val result = for {
        _ <- maybeClearSimplifiedExpensesTailoringAnswers(context)
      } yield Redirect(journeys.income.routes.IncomeCYAController.onPageLoad(taxYear, businessId))

      handleResultT(result)
  }

}
