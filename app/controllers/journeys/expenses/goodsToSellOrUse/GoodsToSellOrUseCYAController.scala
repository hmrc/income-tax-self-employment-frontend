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

package controllers.journeys.expenses.goodsToSellOrUse

import controllers.actions._
import models.NormalMode
import models.common.ModelUtils.userType
import navigation.ExpensesNavigator
import pages.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountSummary
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAView

import javax.inject.Inject

class GoodsToSellOrUseCYAController @Inject() (override val messagesApi: MessagesApi,
                                               navigator: ExpensesNavigator,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: GoodsToSellOrUseCYAView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int, businessId: String): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val nextRoute = navigator
      .nextPage(GoodsToSellOrUseCYAPage, NormalMode, request.userAnswers, taxYear, businessId)
      .url

    val summaryList = SummaryList(
      rows = Seq(
        GoodsToSellOrUseAmountSummary.row(request.userAnswers, userType(request.user.isAgent), taxYear, businessId),
      ).flatten,
      classes = "govuk-!-margin-bottom-7"
    )

    Ok(view(taxYear, userType(request.user.isAgent), summaryList, nextRoute))
  }

}
