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
import controllers.returnAccountingType
import forms.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.goodsToSellOrUse.{GoodsToSellOrUseAmountPage, TaxiMinicabOrRoadHaulagePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsToSellOrUseAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  navigator: ExpensesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: GoodsToSellOrUseAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: GoodsToSellOrUseAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm =
        request.getValue(GoodsToSellOrUseAmountPage, businessId).fold(formProvider(request.userType))(formProvider(request.userType).fill)
      val taxiDriver = request.getValue(TaxiMinicabOrRoadHaulagePage, businessId).contains(true)
      Ok(view(preparedForm, mode, request.userType, taxYear, businessId, returnAccountingType(businessId), taxiDriver))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val taxiDriver = request.getValue(TaxiMinicabOrRoadHaulagePage, businessId).contains(true)
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, returnAccountingType(businessId), taxiDriver))),
          value =>
            selfEmploymentService
              .persistAnswer(businessId, request.userAnswers, value, GoodsToSellOrUseAmountPage)
              .map(updatedAnswers => Redirect(navigator.nextPage(GoodsToSellOrUseAmountPage, mode, updatedAnswers, taxYear, businessId)))
        )
  }

}
