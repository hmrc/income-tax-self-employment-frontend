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

import controllers.actions._
import forms.expenses.officeSupplies.OfficeSuppliesDisallowableAmountFormProvider
import models.Mode
import models.common.ModelUtils.userType
import navigation.ExpensesNavigator
import pages.expenses.officeSupplies.OfficeSuppliesDisallowableAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val allowableAmount = BigDecimal.decimal(1000.00) // This needs to be made dynamic

    val preparedForm = request.userAnswers.get(OfficeSuppliesDisallowableAmountPage) match {
      case None        => formProvider(userType(request.user.isAgent), allowableAmount)
      case Some(value) => formProvider(userType(request.user.isAgent), allowableAmount).fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val allowableAmount = BigDecimal.decimal(1000.00) // This needs to be made dynamic

    formProvider(userType(request.user.isAgent), allowableAmount)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(OfficeSuppliesDisallowableAmountPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OfficeSuppliesDisallowableAmountPage, mode, updatedAnswers))
      )
  }

}
