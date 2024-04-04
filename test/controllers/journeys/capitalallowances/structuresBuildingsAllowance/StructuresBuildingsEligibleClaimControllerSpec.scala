package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.BusinessId
import models.requests.DataRequest
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

  override def onPageLoadCall: Call = routes.StructuresBuildingsEligibleClaimController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.StructuresBuildingsEligibleClaimController.onSubmit(taxYear, businessId, NormalMode)

  override def onwardRoute: Call = routes.StructuresBuildingsQualifyingUseDateController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
                                                                         request: Request[_],
                                                                         messages: Messages,
                                                                         application: Application): String = {
    val view = application.injector.instanceOf[StructuresBuildingsEligibleClaimView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  mockService.submitBooleanAnswerAndClearDependentAnswers(*[OneQuestionPage[Boolean]], *[BusinessId], *[DataRequest[_]], *) returns pageAnswers
    .set(page, validAnswer, businessId.some)
    .success
    .value
    .asFuture

}

