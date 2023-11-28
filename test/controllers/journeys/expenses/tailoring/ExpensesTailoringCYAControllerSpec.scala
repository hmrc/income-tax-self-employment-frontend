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

import base.CYAOnPageLoadControllerSpec
import base.SpecBase.{stubBusinessId, taxYear}
import controllers.journeys.expenses.tailoring
import models.common.{BusinessId, TaxYear, UserType}
import org.scalatest.prop.TableFor2
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.tailoring.ExpensesTailoringCYAView

class ExpensesTailoringCYAControllerSpec extends CYAOnPageLoadControllerSpec {
  def onPageLoad: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad

  def onPageLoadCases: TableFor2[JsObject, OnPageLoadView] = Table(
    ("userAnswersData", "expectedViews"),
    (JsObject.empty, createExpectedView(taxYear, stubBusinessId, UserType.Individual, SummaryListCYA.summaryListOpt(Nil)))
  )

  def createExpectedView(taxYear: TaxYear, businessId: BusinessId, userType: UserType, summaryList: SummaryList): OnPageLoadView = {
    (msg: Messages, application: Application, request: Request[_]) =>
      val view = application.injector.instanceOf[ExpensesTailoringCYAView]
      view(
        ExpensesTailoringCYAPage.pageName,
        taxYear,
        businessId,
        summaryList,
        userType,
        tailoring.routes.ExpensesTailoringCYAController.onSubmit(taxYear, businessId)
      )(request, msg).toString()
  }
}
