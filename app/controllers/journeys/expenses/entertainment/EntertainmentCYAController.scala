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

package controllers.journeys.expenses.entertainment

import controllers.actions._
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import navigation.ExpensesNavigator
import pages.expenses.entertainment.EntertainmentCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.entertainment.EntertainmentAmountSummary
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.entertainment.EntertainmentCYAView

import javax.inject.Inject

class EntertainmentCYAController @Inject() (override val messagesApi: MessagesApi,
                                            navigator: ExpensesNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: EntertainmentCYAView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator
      .nextPage(EntertainmentCYAPage, NormalMode, request.userAnswers, taxYear, businessId)
      .url

    val summaryList = SummaryListCYA.summaryListOpt(
      List(
        EntertainmentAmountSummary.row(request.userAnswers, taxYear, businessId, request.user.userType)
      )
    )

    Ok(view(taxYear, request.user.userType, summaryList, nextRoute))
  }

}
