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

package controllers.journeys.expenses.advertisingOrMarketing

import controllers.actions._
import controllers.standard.routes
import forms.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.advertisingOrMarketing.{AdvertisingOrMarketingAmountPage, AdvertisingOrMarketingDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.advertisingOrMarketing.AdvertisingDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdvertisingDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                         selfEmploymentService: SelfEmploymentService,
                                                         navigator: ExpensesNavigator,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         formProvider: AdvertisingDisallowableAmountFormProvider,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: AdvertisingDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request.userAnswers.get(AdvertisingOrMarketingAmountPage, Some(businessId)) match {
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(amount) =>
          val preparedForm =
            request.userAnswers.get(AdvertisingOrMarketingDisallowableAmountPage, Some(businessId)) match {
              case None        => formProvider(request.userType, amount)
              case Some(value) => formProvider(request.userType, amount).fill(value)
            }

          Future.successful(Ok(view(preparedForm, mode, request.userType, taxYear, businessId, formatMoney(amount))))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request.userAnswers.get(AdvertisingOrMarketingAmountPage, Some(businessId)) match {
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(amount) =>
          formProvider(request.userType, amount)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(amount)))),
              value =>
                selfEmploymentService
                  .persistAnswer(businessId, request.userAnswers, value, AdvertisingOrMarketingDisallowableAmountPage)
                  .map(updatedAnswers =>
                    Redirect(navigator.nextPage(AdvertisingOrMarketingDisallowableAmountPage, mode, updatedAnswers, taxYear, businessId)))
            )
      }
  }

}
