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

import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import navigation.IncomeNavigator
import pages.income.NotTaxableAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.income.NotTaxableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotTaxableAmountController @Inject() (override val messagesApi: MessagesApi,
                                            selfEmploymentService: SelfEmploymentService,
                                            navigator: IncomeNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: CurrencyFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: NotTaxableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = NotTaxableAmountPage
  private val form = (userType: UserType) =>
    formProvider(
      page,
      userType,
      minValueError = s"notTaxableAmount.error.lessThanZero.$userType",
      maxValueError = s"notTaxableAmount.error.overMax.$userType",
      nonNumericError = s"notTaxableAmount.error.nonNumeric.$userType"
    )

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val filledForm = fillForm(page, businessId, form(request.userType))
      Ok(view(filledForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      form(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            selfEmploymentService
              .persistAnswer(businessId, request.userAnswers, value, NotTaxableAmountPage)
              .map(updatedAnswers => Redirect(navigator.nextPage(NotTaxableAmountPage, mode, updatedAnswers, taxYear, businessId)))
        )
  }

}
