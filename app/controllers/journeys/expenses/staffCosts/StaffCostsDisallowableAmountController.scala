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

package controllers.journeys.expenses.staffCosts

import cats.implicits.toBifunctorOps
import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.staffCosts.StaffCostsDisallowableAmountFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, TextAmount}
import models.requests.DataRequest
import navigation.ExpensesNavigator
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.staffCosts.StaffCostsDisallowableAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StaffCostsDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                        selfEmploymentService: SelfEmploymentService,
                                                        navigator: ExpensesNavigator,
                                                        identify: IdentifierAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: StaffCostsDisallowableAmountFormProvider,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        view: StaffCostsDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getStaffCostsAmount(businessId).map { allowableAmount =>
        val preparedForm = request.userAnswers.get(StaffCostsDisallowableAmountPage, Some(businessId)) match {
          case Some(existingAnswer) => formProvider(request.userType, allowableAmount).fill(existingAnswer)
          case None                 => formProvider(request.userType, allowableAmount)
        }
        Ok(view(preparedForm, mode, request.userType, taxYear, businessId, TextAmount(allowableAmount)))
      }.merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getStaffCostsAmount(businessId)
        .map { staffCostsAmount =>
          formProvider(request.userType, staffCostsAmount)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, TextAmount(staffCostsAmount)))),
              value =>
                selfEmploymentService
                  .persistAnswer(businessId, request.userAnswers, value, StaffCostsDisallowableAmountPage)
                  .map(updated => Redirect(navigator.nextPage(StaffCostsDisallowableAmountPage, mode, updated, taxYear, businessId)))
            )
        }
        .leftMap(Future.successful)
        .merge
  }

  private def getStaffCostsAmount(businessId: BusinessId)(implicit request: DataRequest[AnyContent]): Either[Result, BigDecimal] =
    request.userAnswers.get(StaffCostsAmountPage, Some(businessId)).toRight(Redirect(JourneyRecoveryController.onPageLoad()))

}
