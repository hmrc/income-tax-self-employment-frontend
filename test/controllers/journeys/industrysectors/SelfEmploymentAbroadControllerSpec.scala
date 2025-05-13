package controllers.journeys.industrysectors

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import models.NormalMode
import pages.industrysectors.SelfEmploymentAbroadPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.industrysectors.SelfEmploymentAbroadView

class SelfEmploymentAbroadControllerSpec extends BooleanGetAndPostQuestionBaseSpec("SelfEmploymentAbroadController", SelfEmploymentAbroadPage) {

  override def onPageLoadCall: Call = routes.SelfEmploymentAbroadController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.SelfEmploymentAbroadController.onSubmit(taxYear, businessId, NormalMode)

  override def expectedView(form: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[SelfEmploymentAbroadView]
    view(form, scenario.taxYear, scenario.businessId, scenario.userType, scenario.mode).toString()
  }

}
