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

package controllers.journeys.abroad

import controllers.actions._
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.abroad.SelfEmploymentAbroadCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.SelfEmploymentAbroadSummary
import views.html.SelfEmploymentAbroadCYAView

import javax.inject.Inject

class SelfEmploymentAbroadCYAController @Inject()(override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  navigator: Navigator,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: SelfEmploymentAbroadCYAView)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val isAgent = request.user.isAgent
    val summaryListRows = SelfEmploymentAbroadSummary.row(taxYear, isAgent, "SJPR05893938418", request.userAnswers) //TODO Connie pass in BusinessID
    val summaryList = SummaryList(Seq(summaryListRows))

    val nextRoute = nextPageUrl(taxYear, navigator)

    Ok(view(taxYear, summaryList, nextRoute, isAgent))
  }

  private def nextPageUrl(taxYear: Int, navigator: Navigator)(implicit request: DataRequest[AnyContent]): String =
    navigator
      .nextPage(SelfEmploymentAbroadCYAPage, NormalMode, request.userAnswers, taxYear)
      .url

}
