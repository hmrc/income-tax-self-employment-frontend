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
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding
import pages.capitalallowances.structuresBuildingsAllowance.{NewStructuresBuildingsList, StructuresBuildingsRemovePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsRemoveView

import java.time.LocalDate

class StructuresBuildingsRemoveControllerSpec
    extends BooleanGetAndPostQuestionBaseSpec("StructuresBuildingsRemoveController", StructuresBuildingsRemovePage) {

  override val checkForExistingAnswers = false
  override def onPageLoadCall: Call    = routes.StructuresBuildingsRemoveController.onPageLoad(taxYear, businessId, 0)
  override def onSubmitCall: Call      = routes.StructuresBuildingsRemoveController.onSubmit(taxYear, businessId, 0)

  override def onwardRoute: Call = routes.StructuresBuildingsNewStructuresController.onPageLoad(taxYear, businessId)

  override def baseAnswers: UserAnswers = buildUserAnswers(NewStructuresBuildingsList, List(NewStructureBuilding(Some(LocalDate.of(2020, 2, 2)))))

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[StructuresBuildingsRemoveView]
    view(form, scenario.userType, scenario.taxYear, scenario.businessId, 0).toString()
  }

}
