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
import models.database.UserAnswers
import navigation.ExpensesNavigator
import pages.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountPage, GoodsToSellOrUseAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.goodsToSellOrUse.DisallowableGoodsToSellOrUseAmountView

import java.time.LocalDate
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

  val businessId = "SJPR05893938418"
  val taxYear = LocalDate.now.getYear

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val goodsAmount = request.userAnswers
      .getOrElse(UserAnswers(request.userId))
      .get(GoodsToSellOrUseAmountPage, Some(businessId)).getOrElse(BigDecimal(0))
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(DisallowableGoodsToSellOrUseAmountPage) match {
      case None => formProvider(userType(request.user.isAgent), goodsAmount)
      case Some(value) => formProvider(userType(request.user.isAgent), goodsAmount).fill(value)
    }

    Ok(view(preparedForm, mode, userType(request.user.isAgent), taxYear, businessId))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    val goodsAmount = request.userAnswers.getOrElse(UserAnswers(request.userId))
      .get(GoodsToSellOrUseAmountPage, Some(businessId)).getOrElse(BigDecimal(0))
    formProvider(userType(request.user.isAgent), goodsAmount)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(
          BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.getOrElse(UserAnswers(request.userId)).set(DisallowableGoodsToSellOrUseAmountPage, value, Some(businessId)))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DisallowableGoodsToSellOrUseAmountPage, mode, updatedAnswers))
      )
  }

}
