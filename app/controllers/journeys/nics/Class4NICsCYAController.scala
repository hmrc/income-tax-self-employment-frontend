/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.nics

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.journeys
import models.NormalMode
import models.common.BusinessId.nationalInsuranceContributions
import models.common.TaxYear
import models.journeys.Journey.NationalInsuranceContributions
import pages.Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}

@Singleton
class Class4NICsCYAController @Inject() (override val messagesApi: MessagesApi,
                                         val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getAnswers: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         view: CheckYourAnswersView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear): Action[AnyContent] = (identify andThen getAnswers andThen requireData) { implicit request =>
    val summaryList = SummaryListCYA.summaryListOpt(List())

    Ok(
      view(
        Page.cyaHeadingKeyPrefix,
        taxYear,
        request.userType,
        summaryList,
        routes.Class4NICsCYAController.onSubmit(taxYear)
      )
    )
  }

  def onSubmit(taxYear: TaxYear): Action[AnyContent] = (identify andThen getAnswers andThen requireData) { _ =>
    Redirect(
      journeys.routes.SectionCompletedStateController
        .onPageLoad(taxYear, nationalInsuranceContributions, NationalInsuranceContributions.entryName, NormalMode))
  }
}
