package controllers.journeys.adjustments.profitOrLoss

import base.questionPages.CheckboxControllerBaseSpec
import forms.standard.EnumerableFormProvider
import models.{Mode, NormalMode}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
import org.mockito.IdiomaticMockito.StubbingOps
import pages.OneQuestionPage
import pages.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Request}
import views.html.journeys.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossView

class WhatDoYouWantToDoWithLossControllerSpec extends CheckboxControllerBaseSpec("WhatDoYouWantToDoWithLossController", WhatDoYouWantToDoWithLossPage) {

  override def answer: WhatDoYouWantToDoWithLoss = DeductFromOtherTypes

  override def onPageLoadRoute: String = routes.WhatDoYouWantToDoWithLossController.onPageLoad(taxYear, businessId, NormalMode).url

  override def onSubmitRoute: String = routes.WhatDoYouWantToDoWithLossController.onSubmit(taxYear, businessId, NormalMode).url

  override def onwardRoute: Call = routes.PreviousUnusedLossesController.onPageLoad(taxYear, businessId, NormalMode)

  override def createForm(userType: UserType): Form[WhatDoYouWantToDoWithLoss] = new EnumerableFormProvider()(WhatDoYouWantToDoWithLossPage, userType)

  mockService.submitGatewayQuestionAndRedirect(
    *[OneQuestionPage[WhatDoYouWantToDoWithLoss]],
    *[BusinessId],
    *[UserAnswers],
    *,
    *[TaxYear],
    *[Mode]) returns Redirect(onwardRoute).asFuture

  override def expectedView(expectedForm: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WhatDoYouWantToDoWithLossView]
    view(expectedForm, taxYear, businessId, scenario.userType, scenario.mode).toString()
  }
}
