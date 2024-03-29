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

import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsAllowanceFormProvider
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.requests.DataRequest
import models.{Mode, NormalMode}
import navigation.CapitalAllowancesNavigator
import pages.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsAllowancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.Settable
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsAllowanceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StructuresBuildingsAllowanceController @Inject() (override val messagesApi: MessagesApi,
                                                        navigator: CapitalAllowancesNavigator,
                                                        identify: IdentifierAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        service: SelfEmploymentService,
                                                        formProvider: StructuresBuildingsAllowanceFormProvider,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        view: StructuresBuildingsAllowanceView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = request.userAnswers
        .get(StructuresBuildingsAllowancePage, businessId.some)
        .fold(formProvider(request.userType))(formProvider(request.userType).fill)

      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, mode, request.userType, taxYear, businessId))),
          answer =>
            for {
              (editedUserAnswers, redirectMode) <- handleGatewayQuestion(answer, request, mode, businessId)
              updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, answer, StructuresBuildingsAllowancePage)
            } yield Redirect(navigator.nextPage(StructuresBuildingsAllowancePage, redirectMode, updatedUserAnswers, taxYear, businessId))
        )
  }

  private def handleGatewayQuestion(currentAnswer: Boolean,
                                    request: DataRequest[_],
                                    mode: Mode,
                                    businessId: BusinessId): Future[(UserAnswers, Mode)] = {
    val pagesToBeCleared: List[Settable[_]] =
      List( // TODO SASS-7231 add pages to be cleared
      )
    val clearUserAnswerDataIfNeeded = currentAnswer match {
      case false => Future.fromTry(clearDataFromUserAnswers(request.userAnswers, pagesToBeCleared, Some(businessId)))
      case true  => Future(request.userAnswers)
    }
    val redirectMode = request.getValue(StructuresBuildingsAllowancePage, businessId) match {
      case Some(false) if currentAnswer => NormalMode
      case _                            => mode
    }
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

}
