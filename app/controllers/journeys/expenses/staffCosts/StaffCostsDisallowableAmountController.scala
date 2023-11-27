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
import models.common.ModelUtils.userType
import models.common.{BusinessId, TaxYear, TextAmount}
import models.requests.DataRequest
import navigation.ExpensesNavigator
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.staffCosts.StaffCostsDisallowableAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StaffCostsDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                        sessionRepository: SessionRepository,
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
        val preparedForm = request.userAnswers.get(StaffCostsDisallowableAmountPage, Some(businessId.value)) match {
          case Some(existingAnswer) => formProvider(userType(request.user.isAgent), allowableAmount).fill(existingAnswer)
          case None                 => formProvider(userType(request.user.isAgent), allowableAmount)
        }
        Ok(view(preparedForm, mode, request.userType, taxYear, businessId, TextAmount(allowableAmount)))
      }.merge
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getStaffCostsAmount(businessId)
        .map { staffCostsAmount =>
          formProvider(userType(request.user.isAgent), staffCostsAmount)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, TextAmount(staffCostsAmount)))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(StaffCostsDisallowableAmountPage, value, Some(businessId.value)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(StaffCostsDisallowableAmountPage, mode, updatedAnswers, taxYear, businessId))
            )
        }
        .leftMap(Future.successful)
        .merge
  }

  private def getStaffCostsAmount(businessId: BusinessId)(implicit request: DataRequest[AnyContent]): Either[Result, BigDecimal] =
    request.userAnswers.get(StaffCostsAmountPage, Some(businessId.value)).toRight(Redirect(JourneyRecoveryController.onPageLoad()))

}
