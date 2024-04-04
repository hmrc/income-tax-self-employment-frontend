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
import pages.capitalallowances.specialTaxSites.{ContractForBuildingConstructionPage, SpecialTaxSitesBasePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import services.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesService
import views.html.journeys.capitalallowances.specialTaxSites.ContractForBuildingConstructionView

class ContractForBuildingConstructionControllerSpec
    extends BooleanGetAndPostQuestionBaseSpec("ContractForBuildingConstructionController", ContractForBuildingConstructionPage) {

  override def onPageLoadCall: Call = routes.ContractForBuildingConstructionController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def onSubmitCall: Call = routes.ContractForBuildingConstructionController.onSubmit(taxYear, businessId, 0, NormalMode)

  override def onwardRoute: Call = routes.ContractStartDateController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def baseAnswers: UserAnswers = buildUserAnswers(Json.obj("specialTaxSites" -> true))

  override def pageAnswers: UserAnswers = buildUserAnswers(
    Json.obj("specialTaxSites" -> true, "newSpecialTaxSites" -> List(Json.obj("contractForBuildingConstruction" -> Some(validAnswer)))))

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ContractForBuildingConstructionView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, 0).toString()
  }

  val mockStsService: SpecialTaxSitesService = mock[SpecialTaxSitesService]
  mockStsService.updateSiteAnswerWithIndex(*[UserAnswers], *, *[BusinessId], *, *[SpecialTaxSitesBasePage[Boolean]]) returns pageAnswers
    .set(page, validAnswer, businessId.some)
    .success
    .value
    .asFuture

}
