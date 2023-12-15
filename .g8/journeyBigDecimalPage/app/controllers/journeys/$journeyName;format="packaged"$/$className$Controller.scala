package controllers.journeys.$journeyName$

import controllers.actions._
import forms.$journeyName$.$className$FormProvider
import models.Mode
import models.common.TaxYear
import models.requests.DataRequest
import navigation.$journeyName;format="cap"$Navigator
import pages.$journeyName$.$className$Page
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.$journeyName$.$className$View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class $className$Controller @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: $journeyName;format="cap"$Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: $className$FormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: $className$View
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(implicit request: DataRequest[AnyContent]) = formProvider(request.userType)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get($className$Page) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set($className$Page, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage($className$Page, mode, updatedAnswers))
      )
  }
}
