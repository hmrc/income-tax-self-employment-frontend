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
import forms.expenses.construction.ConstructionIndustryAmountFormProvider
import models.Mode
import models.common.ModelUtils.userType
import navigation.ExpensesNavigator
import pages.expenses.construction.ConstructionIndustryAmountPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.construction.ConstructionIndustryAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConstructionIndustryAmountController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: ExpensesNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ConstructionIndustryAmountFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ConstructionIndustryAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(ConstructionIndustryAmountPage, Some(businessId)) match {
        case None        => formProvider(userType(request.user.isAgent))
        case Some(value) => formProvider(userType(request.user.isAgent)).fill(value)
      }

      Ok(view(preparedForm, mode, userType(request.user.isAgent), taxYear, businessId))
  }

  def onSubmit(taxYear: Int, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(userType(request.user.isAgent))
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userType(request.user.isAgent), taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ConstructionIndustryAmountPage, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ConstructionIndustryAmountPage, mode, updatedAnswers, taxYear, businessId))
        )
  }
}
