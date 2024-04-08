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

package controllers.journeys.expenses.construction

import controllers.actions._
import controllers.journeys.fillForm
import forms.expenses.construction.ConstructionIndustryDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.expenses.construction.{ConstructionIndustryAmountPage, ConstructionIndustryDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.construction.ConstructionIndustryDisallowableAmountView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConstructionIndustryDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  service: SelfEmploymentService,
                                                                  identify: IdentifierAction,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  formProvider: ConstructionIndustryDisallowableAmountFormProvider,
                                                                  view: ConstructionIndustryDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = ConstructionIndustryDisallowableAmountPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (for {
        allowableAmount <- request.valueOrRedirectDefault(ConstructionIndustryAmountPage, businessId)
        form = fillForm(page, businessId, formProvider(request.userType, allowableAmount))
      } yield Ok(view(form, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))).merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      request
        .valueOrFutureRedirectDefault(ConstructionIndustryAmountPage, businessId)
        .map { allowableAmount =>
          formProvider(request.userType, allowableAmount)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, formatMoney(allowableAmount)))),
              value => service.persistAnswerAndRedirect(page, businessId, request, value, taxYear, mode)
            )
        }
        .merge
  }

}
