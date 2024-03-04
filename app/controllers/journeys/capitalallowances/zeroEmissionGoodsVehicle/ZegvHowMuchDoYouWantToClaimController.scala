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

package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider.ZegvHowMuchDoYouWantToClaimModel
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.calculateFullCost
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim._
import models.requests.DataRequest
import pages.capitalallowances.zeroEmissionGoodsVehicle.{
  ZegvClaimAmountPage,
  ZegvHowMuchDoYouWantToClaimPage,
  ZegvTotalCostOfVehiclePage,
  ZegvUseOutsideSEPercentagePage
}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZegvHowMuchDoYouWantToClaimController @Inject() (override val messagesApi: MessagesApi,
                                                       service: SelfEmploymentService,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: ZegvHowMuchDoYouWantToClaimView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val calculatedCost = calcFullCost(request, businessId)
      calculatedCost.fold(ZegvHowMuchDoYouWantToClaimPage.redirectToRecoveryPage) { fullCost =>
        val formProvider            = ZegvHowMuchDoYouWantToClaimFormProvider(request.userType, fullCost)
        val howMuchDoYouWantToClaim = request.getValue(ZegvHowMuchDoYouWantToClaimPage, businessId)
        val totalCost               = request.getValue(ZegvClaimAmountPage, businessId)
        val filledForm = (howMuchDoYouWantToClaim, totalCost) match {
          case (Some(claim), Some(totalCost)) if claim == LowerAmount =>
            formProvider.fill(ZegvHowMuchDoYouWantToClaimModel(claim, Some(totalCost)))
          case (Some(claim), _) =>
            formProvider.fill(ZegvHowMuchDoYouWantToClaimModel(claim, None))
          case _ => formProvider
        }

        Ok(view(filledForm, mode, request.userType, taxYear, businessId, fullCost))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: ZegvHowMuchDoYouWantToClaimModel, fullCost: BigDecimal): Future[Result] = {
        val totalCostOfCar: BigDecimal = answer.howMuchDoYouWantToClaim match {
          case FullCost    => fullCost
          case LowerAmount => answer.totalCost.getOrElse(0)
        }
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(ZegvHowMuchDoYouWantToClaimPage, answer.howMuchDoYouWantToClaim, Some(businessId)))
          finalAnswers   <- service.persistAnswer(businessId, updatedAnswers, totalCostOfCar, ZegvClaimAmountPage)
        } yield ZegvHowMuchDoYouWantToClaimPage.redirectNext(mode, finalAnswers, businessId, taxYear)
      }

      val calculatedCost = calcFullCost(request, businessId)
      calculatedCost.fold(Future(ZegvHowMuchDoYouWantToClaimPage.redirectToRecoveryPage)) { fullCost =>
        ZegvHowMuchDoYouWantToClaimFormProvider(request.userType, fullCost)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, fullCost))),
            value => handleSuccess(value, fullCost)
          )
      }
  }

  private def calcFullCost(request: DataRequest[AnyContent], businessId: BusinessId) =
    calculateFullCost(ZegvUseOutsideSEPercentagePage, ZegvTotalCostOfVehiclePage, request, businessId)

}
