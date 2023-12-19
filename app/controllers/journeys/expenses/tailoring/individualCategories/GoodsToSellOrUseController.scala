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

package controllers.journeys.expenses.tailoring.individualCategories

import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.tailoring.individualCategories.GoodsToSellOrUseFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.expenses.individualCategories.TaxiMinicabOrRoadHaulage
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.{GoodsToSellOrUsePage, TaxiMinicabOrRoadHaulagePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.tailoring.individualCategories.GoodsToSellOrUseView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsToSellOrUseController @Inject() (override val messagesApi: MessagesApi,
                                            selfEmploymentService: SelfEmploymentService,
                                            sessionRepository: SessionRepository,
                                            navigator: ExpensesTailoringNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: GoodsToSellOrUseFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: GoodsToSellOrUseView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) map {
        case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
        case Right(accountingType) =>
          val preparedForm = request.userAnswers.get(GoodsToSellOrUsePage, Some(businessId)) match {
            case None        => formProvider(request.userType)
            case Some(value) => formProvider(request.userType).fill(value)
          }
          val taxiDriver = request.userAnswers
            .get(TaxiMinicabOrRoadHaulagePage, Some(businessId))
            .contains(TaxiMinicabOrRoadHaulage.Yes)
          Ok(view(preparedForm, mode, request.userType, taxYear, businessId, accountingType, taxiDriver))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
        case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
        case Right(accountingType) =>
          val taxiDriver = request.userAnswers
            .get(TaxiMinicabOrRoadHaulagePage, Some(businessId))
            .contains(TaxiMinicabOrRoadHaulage.Yes)
          val form = formProvider(request.userType)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, accountingType, taxiDriver))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(GoodsToSellOrUsePage, value, Some(businessId)))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(GoodsToSellOrUsePage, mode, updatedAnswers, taxYear, businessId))
            )
      }
  }

}
