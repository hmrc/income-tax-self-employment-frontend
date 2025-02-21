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

import base.questionPages.LocalDateGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.structuresBuildingsAllowance.{StructuresBuildingsBasePage, StructuresBuildingsQualifyingUseDatePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Request}
import services.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsService
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsQualifyingUseDateView

import java.time.LocalDate

class StructuresBuildingsQualifyingUseDateControllerSpec
    extends LocalDateGetAndPostQuestionBaseSpec("StructuresBuildingsQualifyingUseDateController", StructuresBuildingsQualifyingUseDatePage) {

  override def onPageLoadRoute: String = routes.StructuresBuildingsQualifyingUseDateController.onPageLoad(taxYear, businessId, 0, NormalMode).url

  override def onSubmitRoute: String = routes.StructuresBuildingsQualifyingUseDateController.onSubmit(taxYear, businessId, 0, NormalMode).url

  override def onwardRoute: Call = routes.StructuresBuildingQualifyingExpenditureController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def validDate: LocalDate = LocalDate.of(2020, 2, 2)

  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj("structuresBuildingsAllowance" -> true, "structuresBuildingsEligibleClaim" -> true))

  override def pageAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "structuresBuildingsAllowance"     -> true,
      "structuresBuildingsEligibleClaim" -> true,
      "newStructuresBuildings"           -> List(Json.obj("qualifyingUse" -> Some("2020-02-02")))
    ))

  override def createForm(userType: UserType): Form[LocalDate] =
    form(
      page,
      userType,
      userSpecificRequiredError = true,
      latestDateAndError = Some((mockTimeMachine.now, "structuresBuildingsQualifyingUseDate.error.inFuture")))

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[StructuresBuildingsQualifyingUseDateView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, 0).toString()
  }

  val mockSbsService: StructuresBuildingsService = mock[StructuresBuildingsService]
  mockSbsService.updateAndRedirectWithIndex(
    *[UserAnswers],
    *[LocalDate],
    *[BusinessId],
    *[TaxYear],
    *,
    *[StructuresBuildingsBasePage[LocalDate]]) returns Redirect(onwardRoute).asFuture

}
