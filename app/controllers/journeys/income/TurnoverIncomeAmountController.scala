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

import cats.implicits.catsSyntaxOptionId
import controllers.actions._
import controllers.journeys.fillForm
import controllers.returnAccountingType
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, MoneyBounds, TaxYear, UserType}
import pages.income.TurnoverIncomeAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.income.TurnoverIncomeAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class TurnoverIncomeAmountController @Inject() (override val messagesApi: MessagesApi,
                                                val controllerComponents: MessagesControllerComponents,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                service: SelfEmploymentService,
                                                formProvider: CurrencyFormProvider,
                                                view: TurnoverIncomeAmountView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = TurnoverIncomeAmountPage
  private val form = (userType: UserType) => formProvider(page, userType, prefix = page.toString.some, minValue = MoneyBounds.minimumZeroOrOverValue)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = fillForm(page, businessId, form(request.userType))
      Ok(view(filledForm, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      form(request.userType)
        .bindFromRequest()
        .fold(
          formErrors =>
            Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId, returnAccountingType(businessId)))),
          answer => service.persistAnswerAndRedirect(page, businessId, request, answer, taxYear, mode)
        )
  }

}
