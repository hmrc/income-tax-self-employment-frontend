package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvUseOutsideSEFormProvider.ZegvUseOutsideSEFormModel
import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim._
import models.requests.DataRequest
import pages.capitalallowances.zeroEmissionGoodsVehicle.{ZegvUseOutsideSEPage, ZegvUseOutsideSEPercentagePage}
import play.api.mvc.AnyContent
import services.SelfEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZegvUseOutsideSEService @Inject() (service: SelfEmploymentService)(implicit ec: ExecutionContext) {

  def submitAnswer(request: DataRequest[AnyContent], newAnswers: ZegvUseOutsideSEFormModel, businessId: BusinessId): Future[UserAnswers] = {
    val userAnswers = request.userAnswers
    for {
      updatedAnswers     <- Future.fromTry(userAnswers.set(ZegvUseOutsideSEPage, newAnswers.radioPercentage, Some(businessId)))
      updatedUserAnswers <- service.persistAnswer(businessId, updatedAnswers, newAnswers.optDifferentAmount, ZegvUseOutsideSEPercentagePage)
    } yield updatedUserAnswers
  }

}
