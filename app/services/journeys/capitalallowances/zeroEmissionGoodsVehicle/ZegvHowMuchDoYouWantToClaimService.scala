package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider.ZegvHowMuchDoYouWantToClaimModel
import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim._
import pages.capitalallowances.zeroEmissionGoodsVehicle.{ZegvClaimAmountPage, ZegvHowMuchDoYouWantToClaimPage}
import services.SelfEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZegvHowMuchDoYouWantToClaimService @Inject() (service: SelfEmploymentService)(implicit ec: ExecutionContext) {

  def submitAnswer(userAnswers: UserAnswers,
                   newAnswers: ZegvHowMuchDoYouWantToClaimModel,
                   fullCost: BigDecimal,
                   businessId: BusinessId): Future[UserAnswers] = {
    val totalCostOfCar: BigDecimal = newAnswers.howMuchDoYouWantToClaim match {
      case FullCost    => fullCost
      case LowerAmount => newAnswers.totalCost.getOrElse(0)
    }

    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(ZegvHowMuchDoYouWantToClaimPage, newAnswers.howMuchDoYouWantToClaim, Some(businessId)))
      finalAnswers   <- service.persistAnswer(businessId, updatedAnswers, totalCostOfCar, ZegvClaimAmountPage)
    } yield finalAnswers
  }
}
