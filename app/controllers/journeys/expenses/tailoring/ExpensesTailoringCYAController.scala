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
import forms.expenses.tailoring.ExpensesTailoringCYAFormProvider
import models.common.{BusinessId, TaxYear}
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.tailoring.AdvertisingOrMarketingSummary
import views.html.journeys.expenses.tailoring.ExpensesTailoringCYAView
import controllers.journeys.expenses.tailoring

import javax.inject.Inject
import scala.annotation.nowarn

class ExpensesTailoringCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ExpensesTailoringCYAFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ExpensesTailoringCYAView
) extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userType = request.userType

    val summaryList = SummaryList(
      rows = Seq( // TODO Add during Expenses tailoring CYA story
      ).flatten,
      classes = "govuk-!-margin-bottom-7"
    )

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

  @nowarn
  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      ??? // TODO SASS-6339
  }
}
