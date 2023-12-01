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

import base.SpecBase.{businessId, taxYear, userAnswersId}
import base.{CYAOnPageLoadControllerSpec, SpecBase}
import builders.ExpensesTailoringJsonBuilder
import common.TestApp.buildAppFromUserAnswers
import controllers.journeys.expenses.tailoring
import models.common.UserType.Individual
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import org.scalatest.prop.TableFor2
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.tailoring.buildTailoringSummaryList
import views.html.standard.CheckYourAnswersView

class ExpensesTailoringCYAControllerSpec extends CYAOnPageLoadControllerSpec {

  val userAnswers: UserAnswers = UserAnswers(userAnswersId, Json.obj(businessId.value -> ExpensesTailoringJsonBuilder.allNoAnswers))
  val application: Application = buildAppFromUserAnswers(userAnswers)
  implicit val msg: Messages   = SpecBase.messages(application)
  val summaryList: SummaryList = buildTailoringSummaryList(userAnswers, taxYear, businessId, Individual)

  def onPageLoad: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad
  def onPageLoadCases: TableFor2[UserAnswers, OnPageLoadView] = Table(
    ("userAnswers", "expectedViews"),
    (userAnswers, createExpectedView(taxYear, businessId, Individual, summaryList))
  )

  def createExpectedView(taxYear: TaxYear, businessId: BusinessId, userType: UserType, summaryList: SummaryList): OnPageLoadView = {
    (msg: Messages, application: Application, request: Request[_]) =>
      val view = application.injector.instanceOf[CheckYourAnswersView]
      view(
        ExpensesTailoringCYAPage.pageName.value,
        taxYear,
        userType,
        summaryList,
        tailoring.routes.ExpensesTailoringCYAController.onSubmit(taxYear, businessId)
      )(request, msg).toString()
  }
}
