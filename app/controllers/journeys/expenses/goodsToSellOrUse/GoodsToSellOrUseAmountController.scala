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

package controllers.journeys.expenses.goodsToSellOrUse

import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountFormProvider
import models.Mode
import models.common.ModelUtils.userType
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.TaxiMinicabOrRoadHaulage
import navigation.ExpensesNavigator
import pages.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountPage
import pages.expenses.tailoring.TaxiMinicabOrRoadHaulagePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsToSellOrUseAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  sessionRepository: SessionRepository,
                                                  navigator: ExpensesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  formProvider: GoodsToSellOrUseAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: GoodsToSellOrUseAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, BusinessId(businessId), request.user.mtditid) map {
      case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
      case Right(accountingType) =>
        val user = userType(request.user.isAgent)
        val preparedForm =
          request.userAnswers.getOrElse(UserAnswers(request.userId)).get(GoodsToSellOrUseAmountPage, Some(BusinessId(businessId))) match {
            case None        => formProvider(user)
            case Some(value) => formProvider(user).fill(value)
          }
        val taxiDriver = request.userAnswers
          .getOrElse(UserAnswers(request.userId))
          .get(TaxiMinicabOrRoadHaulagePage, Some(BusinessId(businessId)))
          .contains(TaxiMinicabOrRoadHaulage.Yes)
        Ok(view(preparedForm, mode, user, taxYear, businessId, accountingType, taxiDriver))
    }
  }

  def onSubmit(taxYear: TaxYear, businessId: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, BusinessId(businessId), request.user.mtditid) flatMap {
      case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
      case Right(accountingType) =>
        val user = userType(request.user.isAgent)
        val taxiDriver = request.userAnswers
          .getOrElse(UserAnswers(request.userId))
          .get(TaxiMinicabOrRoadHaulagePage, Some(BusinessId(businessId)))
          .contains(TaxiMinicabOrRoadHaulage.Yes)
        val form = formProvider(user)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, user, taxYear, businessId, accountingType, taxiDriver))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.getOrElse(UserAnswers(request.userId)).set(GoodsToSellOrUseAmountPage, value, Some(BusinessId(businessId))))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(GoodsToSellOrUseAmountPage, mode, updatedAnswers, taxYear, BusinessId(businessId)))
          )
    }
  }

}
