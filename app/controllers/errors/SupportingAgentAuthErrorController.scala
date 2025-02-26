package controllers.errors

import config.FrontendAppConfig
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.authErrorPage.SupportingAgentAuthErrorPageView

import javax.inject.Inject

class SupportingAgentAuthErrorController @Inject()(
                                                    val mcc: MessagesControllerComponents,
                                                    implicit val appConfig: FrontendAppConfig,
                                                    view: SupportingAgentAuthErrorPageView
                                                  ) extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = Action { implicit request =>
    Unauthorized(view())
  }
}
