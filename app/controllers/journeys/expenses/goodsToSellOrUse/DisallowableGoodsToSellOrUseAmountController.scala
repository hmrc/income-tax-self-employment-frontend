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

package controllers.journeys.expenses.goodsToSellOrUse

import controllers.actions._
import forms.expenses.goodsToSellOrUse.DisallowableGoodsToSellOrUseAmountFormProvider
import models.Mode
import models.common.ModelUtils.userType
import navigation.ExpensesNavigator
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.goodsToSellOrUse.DisallowableGoodsToSellOrUseAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DisallowableGoodsToSellOrUseAmountController @Inject() (override val messagesApi: MessagesApi,
                                                              sessionRepository: SessionRepository,
                                                              navigator: ExpensesNavigator,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalAction,
                                                              requireData: DataRequiredAction,
                                                              formProvider: DisallowableGoodsToSellOrUseAmountFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: DisallowableGoodsToSellOrUseAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val goodsAmount = request.userAnswers
        .get(GoodsToSellOrUseAmountPage, Some(businessId))
        .getOrElse(BigDecimal(1000.50)) // TODO change this default
      val preparedForm =
        request.userAnswers.get(DisallowableGoodsToSellOrUseAmountPage, Some(businessId)) match {
          case None        => formProvider(userType(request.user.isAgent), goodsAmount)
          case Some(value) => formProvider(userType(request.user.isAgent), goodsAmount).fill(value)
        }

      Ok(view(preparedForm, mode, userType(request.user.isAgent), taxYear, businessId, formatMoney(goodsAmount)))
  }

  def onSubmit(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val goodsAmount = request.userAnswers
        .get(GoodsToSellOrUseAmountPage, Some(businessId))
        .getOrElse(BigDecimal(1000.50))
      formProvider(userType(request.user.isAgent), goodsAmount)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId, formatMoney(goodsAmount)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DisallowableGoodsToSellOrUseAmountPage, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, updatedAnswers, taxYear, businessId))
        )
  }

}
