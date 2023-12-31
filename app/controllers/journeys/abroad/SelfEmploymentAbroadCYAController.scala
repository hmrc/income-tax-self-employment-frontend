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
import models.common.{BusinessId, TaxYear}
import models.requests.DataRequest
import navigation.AbroadNavigator
import pages.abroad.SelfEmploymentAbroadCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.abroad.SelfEmploymentAbroadSummary
import views.html.journeys.abroad.SelfEmploymentAbroadCYAView

import javax.inject.{Inject, Singleton}

@Singleton
class SelfEmploymentAbroadCYAController @Inject() (override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   navigator: AbroadNavigator,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: SelfEmploymentAbroadCYAView)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val summaryListRows = SelfEmploymentAbroadSummary.row(taxYear, request.user.userType, businessId, request.userAnswers)
    val summaryList     = SummaryList(Seq(summaryListRows))

    val nextRoute = nextPageUrl(taxYear, businessId, navigator)

    Ok(view(taxYear, summaryList, nextRoute, request.userType))
  }

  private def nextPageUrl(taxYear: TaxYear, businessId: BusinessId, navigator: AbroadNavigator)(implicit request: DataRequest[AnyContent]): String =
    navigator
      .nextPage(SelfEmploymentAbroadCYAPage, NormalMode, request.userAnswers, taxYear, businessId)
      .url

}
