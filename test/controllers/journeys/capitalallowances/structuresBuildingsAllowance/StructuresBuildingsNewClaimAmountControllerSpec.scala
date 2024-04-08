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

package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsLocation
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.structuresBuildingsAllowance.{StructuresBuildingsBasePage, StructuresBuildingsNewClaimAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import services.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsService
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsNewClaimAmountView

class StructuresBuildingsNewClaimAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("StructuresBuildingsNewClaimAmountController", StructuresBuildingsNewClaimAmountPage) {

  override def onPageLoadRoute: String = routes.StructuresBuildingsNewClaimAmountController.onPageLoad(taxYear, businessId, 0, NormalMode).url

  override def onSubmitRoute: String = routes.StructuresBuildingsNewClaimAmountController.onSubmit(taxYear, businessId, 0, NormalMode).url

  override def onwardRoute: Call = routes.StructuresBuildingsNewStructuresController.onPageLoad(taxYear, businessId)

  override def createForm(userType: UserType): Form[BigDecimal] = form(page, userType)

  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "structuresBuildingsAllowance"     -> true,
      "structuresBuildingsEligibleClaim" -> true,
      "newStructuresBuildings" -> List(
        Json.obj(
          "qualifyingUse"               -> Some("2022-03-02"),
          "structuresBuildingsLocation" -> Some(StructuresBuildingsLocation(Some("name"), None, "AA11AA"))
        ))
    ))

  override def pageAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "structuresBuildingsAllowance"     -> true,
      "structuresBuildingsEligibleClaim" -> true,
      "newStructuresBuildings" -> List(Json.obj(
        "qualifyingUse"                      -> Some("2022-03-02"),
        "structuresBuildingsLocation"        -> Some(StructuresBuildingsLocation(Some("name"), None, "AA11AA")),
        "newStructureBuildingClaimingAmount" -> Some(amount)
      ))
    ))

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[StructuresBuildingsNewClaimAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, 0).toString()
  }

  val mockSbsService: StructuresBuildingsService = mock[StructuresBuildingsService]
  mockSbsService.updateStructureAnswerWithIndex(
    *[UserAnswers],
    *[BigDecimal],
    *[BusinessId],
    *,
    *[StructuresBuildingsBasePage[BigDecimal]]) returns pageAnswers
    .set(page, amount, businessId.some)
    .success
    .value
    .asFuture

}
