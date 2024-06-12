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

package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.BusinessId
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.OneQuestionPage
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsEligibleClaimPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsEligibleClaimView

class StructuresBuildingsEligibleClaimControllerSpec
    extends BooleanGetAndPostQuestionBaseSpec("StructuresBuildingsEligibleClaimController", StructuresBuildingsEligibleClaimPage) {

  override def onPageLoadCall: Call = routes.StructuresBuildingsEligibleClaimController.onPageLoad(taxYear, businessId)
  override def onSubmitCall: Call   = routes.StructuresBuildingsEligibleClaimController.onSubmit(taxYear, businessId)

  override def onwardRoute: Call = routes.StructuresBuildingsQualifyingUseDateController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[StructuresBuildingsEligibleClaimView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  mockService.submitGatewayQuestionAndClearDependentAnswers(*[OneQuestionPage[Boolean]], *[BusinessId], *[UserAnswers], *) returns pageAnswers
    .set(page, validAnswer, businessId.some)
    .success
    .value
    .asFuture

}
