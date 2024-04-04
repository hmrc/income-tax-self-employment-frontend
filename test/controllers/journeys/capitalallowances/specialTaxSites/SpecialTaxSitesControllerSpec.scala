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
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.BusinessId
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.OneQuestionPage
import pages.capitalallowances.specialTaxSites.SpecialTaxSitesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesView

class SpecialTaxSitesControllerSpec extends BooleanGetAndPostQuestionBaseSpec("SpecialTaxSitesController", SpecialTaxSitesPage) {

  override def onPageLoadCall: Call = routes.SpecialTaxSitesController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.SpecialTaxSitesController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = routes.ContractForBuildingConstructionController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[SpecialTaxSitesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  mockService.submitGatewayQuestionAndClearDependentAnswers(*[OneQuestionPage[Boolean]], *[BusinessId], *[UserAnswers], *) returns pageAnswers
    .set(page, validAnswer, businessId.some)
    .success
    .value
    .asFuture

}
