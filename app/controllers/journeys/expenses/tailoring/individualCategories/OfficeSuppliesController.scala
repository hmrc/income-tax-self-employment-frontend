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

package controllers.journeys.expenses.tailoring.individualCategories

import controllers.actions._
import controllers.returnAccountingType
import forms.expenses.tailoring.individualCategories.OfficeSuppliesFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.OfficeSuppliesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.individualCategories.OfficeSuppliesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OfficeSuppliesController @Inject() (override val messagesApi: MessagesApi,
                                          selfEmploymentService: SelfEmploymentService,
                                          navigator: ExpensesTailoringNavigator,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: OfficeSuppliesFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: OfficeSuppliesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      returnAccountingType(selfEmploymentService, request.nino, businessId, request.mtditid) map { accountingType =>
        val preparedForm = request.userAnswers.get(OfficeSuppliesPage, Some(businessId)) match {
          case None        => formProvider(request.userType)
          case Some(value) => formProvider(request.userType).fill(value)
        }

        Ok(view(preparedForm, mode, request.userType, taxYear, businessId, accountingType))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      returnAccountingType(selfEmploymentService, request.nino, businessId, request.mtditid) flatMap { accountingType =>
        val form = formProvider(request.userType)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, accountingType))),
            value =>
              selfEmploymentService
                .persistAnswer(businessId, request.userAnswers, value, OfficeSuppliesPage)
                .map(updatedAnswers => Redirect(navigator.nextPage(OfficeSuppliesPage, mode, updatedAnswers, taxYear, businessId)))
          )
      }
  }

}
