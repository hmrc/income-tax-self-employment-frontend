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
import forms.expenses.tailoring.individualCategories.DepreciationFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.Journey
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.DepreciationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.individualCategories.DepreciationView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DepreciationController @Inject() (override val messagesApi: MessagesApi,
                                        selfEmploymentService: SelfEmploymentService,
                                        navigator: ExpensesTailoringNavigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        hopChecker: HopCheckerAction,
                                        formProvider: DepreciationFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: DepreciationView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private val page = DepreciationPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData
      andThen hopChecker.hasPreviousAnswers(Journey.ExpensesTailoring, page, taxYear, businessId, mode)) { implicit request =>
      val preparedForm = request.userAnswers.get(DepreciationPage, Some(businessId)) match {
        case None        => formProvider(request.userType)
        case Some(value) => formProvider(request.userType).fill(value)
      }

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            selfEmploymentService
              .persistAnswer(businessId, request.userAnswers, value, DepreciationPage)
              .map(updated => Redirect(navigator.nextPage(DepreciationPage, mode, updated, taxYear, businessId)))
        )
  }

}
