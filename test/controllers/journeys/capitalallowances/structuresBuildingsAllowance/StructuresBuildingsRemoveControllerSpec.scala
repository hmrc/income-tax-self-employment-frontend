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

class StructuresBuildingsRemoveControllerSpec extends BooleanGetAndPostQuestionBaseSpec("StructuresBuildingsRemoveController", StructuresBuildingsRemovePage) {

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
