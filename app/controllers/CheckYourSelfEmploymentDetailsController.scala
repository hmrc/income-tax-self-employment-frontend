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

package controllers

import controllers.actions._
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.SelfEmploymentDetailsViewModel._
import views.html.CheckYourSelfEmploymentDetailsView

import javax.inject.Inject

class CheckYourSelfEmploymentDetailsController @Inject()(override val messagesApi: MessagesApi,
                                                         identify: IdentifierAction,
                                                         getData: DataRetrievalAction,
                                                         requireData: DataRequiredAction,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: CheckYourSelfEmploymentDetailsView
                                                        ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData) {
    //  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val userAnswers = UserAnswers(request.userId)
      val isAgent = request.user.isAgent
      val viewModel = SummaryList(
        rows = Seq(
          row(userAnswers, "name", "", Some(isAgent)),
          row(userAnswers, "whatDidYouDo", "", Some(isAgent)),
          row(userAnswers, "accountingType", ""),
          row(userAnswers, "startDate", "", Some(isAgent)),
          row(userAnswers, "linkedToConstructionIndustryScheme", ""),
          row(userAnswers, "fosterCare", "", Some(isAgent)),
          row(userAnswers, "farmerOrMarketGardener", "", Some(isAgent))
        ),
        classes = "govuk-!-margin-bottom-7")
      Ok(view(viewModel, if (isAgent) "agent" else "individual"))
  }
}
