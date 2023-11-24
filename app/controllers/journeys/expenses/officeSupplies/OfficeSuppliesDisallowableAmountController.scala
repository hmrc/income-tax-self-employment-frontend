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

package controllers.journeys.expenses.officeSupplies

import cats.implicits.toBifunctorOps
import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.officeSupplies.OfficeSuppliesDisallowableAmountFormProvider
import models.Mode
import models.common.ModelUtils.userType
import models.common.TaxYear
import models.requests.DataRequest
import navigation.ExpensesNavigator
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesDisallowableAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeSuppliesDisallowableAmountController @Inject() (override val messagesApi: MessagesApi,
                                                            sessionRepository: SessionRepository,
                                                            navigator: ExpensesNavigator,
                                                            identify: IdentifierAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: OfficeSuppliesDisallowableAmountFormProvider,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            view: OfficeSuppliesDisallowableAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with MoneyUtils {

  def onPageLoad(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      obtainAllowableAmount(businessId).map { allowableAmount =>
        val preparedForm = request.userAnswers.get(OfficeSuppliesDisallowableAmountPage, Some(businessId)) match {
          case Some(existingAnswer) => formProvider(userType(request.user.isAgent), allowableAmount).fill(existingAnswer)
          case None                 => formProvider(userType(request.user.isAgent), allowableAmount)
        }
        Ok(view(preparedForm, mode, taxYear, businessId, userType(request.user.isAgent), formatMoney(allowableAmount)))
      }.merge
  }

  def onSubmit(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      obtainAllowableAmount(businessId)
        .map { allowableAmount =>
          formProvider(userType(request.user.isAgent), allowableAmount)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode, taxYear, businessId, userType(request.user.isAgent), formatMoney(allowableAmount)))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(OfficeSuppliesDisallowableAmountPage, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, updatedAnswers, taxYear, businessId))
            )
        }
        .leftMap(Future.successful)
        .merge
  }

  private def obtainAllowableAmount(businessId: String)(implicit request: DataRequest[AnyContent]): Either[Result, BigDecimal] =
    request.userAnswers.get(OfficeSuppliesAmountPage, Some(businessId)).toRight(Redirect(JourneyRecoveryController.onPageLoad()))

}
