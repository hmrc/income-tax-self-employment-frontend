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

package controllers.journeys.capitalallowances.specialTaxSites

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.specialTaxSites.{NewSpecialTaxSitesList, RemoveSpecialTaxSitePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.capitalallowances.specialTaxSites.NewTaxSitesView

class NewTaxSitesControllerSpec extends BooleanGetAndPostQuestionBaseSpec("NewTaxSitesController", RemoveSpecialTaxSitePage) {

  override val checkForExistingAnswers = false

  override def onPageLoadCall: Call = routes.NewTaxSitesController.onPageLoad(taxYear, businessId)

  override def onSubmitCall: Call = routes.NewTaxSitesController.onSubmit(taxYear, businessId)

  override def onwardRoute: Call = routes.ContractForBuildingConstructionController.onPageLoad(taxYear, businessId, 1, NormalMode)

  override def baseAnswers: UserAnswers = buildUserAnswers(NewSpecialTaxSitesList, List(NewSpecialTaxSite(Some(false))))

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[NewTaxSitesView]
    view(form, scenario.userType, scenario.taxYear, scenario.businessId, SummaryListCYA.summaryListOpt(List.empty, None), None).toString()
  }

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns buildUserAnswers(NewSpecialTaxSitesList, List.empty).asFuture

}
