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

import cats.implicits.catsSyntaxOptionId
import controllers.actions._
import controllers.journeys.fillForm
import controllers.returnAccountingType
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
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
                                                  formProvider: CurrencyFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: GoodsToSellOrUseAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = GoodsToSellOrUseAmountPage
  private val form = (userType: UserType) => formProvider(page, userType, prefix = page.toString.some)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = fillForm(page, businessId, form(request.userType))
      val taxiDriver = request.getValue(TaxiMinicabOrRoadHaulagePage, businessId).contains(true)
      Ok(view(filledForm, mode, request.userType, taxYear, businessId, returnAccountingType(businessId), taxiDriver))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val taxiDriver = request.getValue(TaxiMinicabOrRoadHaulagePage, businessId).contains(true)
      form(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, returnAccountingType(businessId), taxiDriver))),
          value =>
            selfEmploymentService
              .persistAnswer(businessId, request.userAnswers, value, page)
              .map(updatedAnswers => Redirect(navigator.nextPage(page, mode, updatedAnswers, taxYear, businessId)))
        )
  }

}
