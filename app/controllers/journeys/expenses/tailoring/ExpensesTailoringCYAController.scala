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

package controllers.journeys.expenses.tailoring

import controllers.actions._
import controllers.handleResult
import controllers.journeys.expenses.tailoring
import models.common.{BusinessId, JourneyContext, TaxYear}
import models.journeys.Journey.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoringAnswers
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.tailoring.ExpensesTailoringCYAView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ExpensesTailoringCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: ExpensesTailoringCYAView,
    service: SelfEmploymentService,
    navigator: ExpensesTailoringNavigator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private implicit val logger: Logger = Logger(this.getClass)

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userType    = request.userType
    val summaryList = SummaryListCYA.summaryListOpt(Nil) // TODO Add in SASS-5829

    Ok(
      view(
        ExpensesTailoringCYAPage.pageName,
        taxYear,
        businessId,
        summaryList,
        userType,
        tailoring.routes.ExpensesTailoringCYAController.onSubmit(taxYear, businessId)
      ))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val nextRoute = navigator.nextNormalRoute(ExpensesTailoringCYAPage, request.userAnswers, taxYear, businessId, accountingType = None).url
      val result = service
        .submitAnswers[ExpensesTailoringAnswers](JourneyContext(taxYear, businessId, request.mtditid, ExpensesTailoring), request.userAnswers)
        .map(_ => Redirect(nextRoute))
        .value

      handleResult(result)
  }
}
