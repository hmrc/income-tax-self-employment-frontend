package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvAllowanceFormProvider
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvAllowancePage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvAllowanceView

class ZegvAllowanceControllerSpec extends RadioButtonGetAndPostQuestionBaseSpec("ZeroEmissionGoodsVehicleController", ZegvAllowancePage) {

  def onPageLoadCall: Call = routes.ZegvAllowanceController.onPageLoad(taxYear, businessId, NormalMode)

  def onSubmitCall: Call = routes.ZegvAllowanceController.onSubmit(taxYear, businessId, NormalMode)

  def onwardRoute: Call = routes.ZegvTotalCostOfVehicleController.onPageLoad(taxYear, businessId, NormalMode)

  def validAnswer: Boolean = true

  def createForm(userType: UserType): Form[Boolean] = new ZegvAllowanceFormProvider()(userType, taxYear)

  def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ZegvAllowanceView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, businessId.some).success.value

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns filledUserAnswers.asFuture
}
