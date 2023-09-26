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

package controllers

import controllers.actions._
import forms.DetailsCompletedSectionFormProvider
import models.DetailsCompletedSection.Yes
import models.{DetailsCompletedSection, Mode, UserAnswers}
import navigation.Navigator
import pages.DetailsCompletedSectionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DetailsCompletedSectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DetailsCompletedSectionController @Inject()(override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  navigator: Navigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  formProvider: DetailsCompletedSectionFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: DetailsCompletedSectionView)
                                                 (implicit val ec: ExecutionContext
                                                 ) extends FrontendBaseController with I18nSupport {

  val form: Form[DetailsCompletedSection] = formProvider()

  def onPageLoad(taxYear: Int, journey: String, mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>

//      val journeyState = selfEmploymentService.getJourneyState(journey+request.userId, journey, taxYear) match {
//        case Right(model) => model.completed
//        case _ => false
//      }
//      TODO update using backend service instead of userAnswers
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(DetailsCompletedSectionPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, journey, mode))
  }

  def onSubmit(taxYear: Int, journey: String, mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, journey, mode))),

        value => {
//          val businessId = journey.toString + "-" + request.nino
          selfEmploymentService.saveJourneyState(journey+request.userId, journey, taxYear, complete = value.equals(Yes)) map {
            case Right(_) => Redirect(navigator.nextPage(DetailsCompletedSectionPage, mode, UserAnswers(request.userId)))
            case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
        }
      )
  }
}
