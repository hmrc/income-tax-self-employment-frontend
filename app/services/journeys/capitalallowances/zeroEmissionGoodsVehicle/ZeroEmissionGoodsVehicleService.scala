package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import controllers.journeys.clearPagesWhenNo
import models.Mode
import models.common.BusinessId
import models.database.UserAnswers
import models.requests.DataRequest
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehiclePage
import services.SelfEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZeroEmissionGoodsVehicleService @Inject() (service: SelfEmploymentService)(implicit ec: ExecutionContext) {

  def submitAnswer(businessId: BusinessId, request: DataRequest[_], newAnswer: Boolean, mode: Mode): Future[(UserAnswers, Mode)] =
    for {
      (editedUserAnswers, redirectMode) <- clearPagesWhenNo(ZeroEmissionGoodsVehiclePage, newAnswer, request, mode, businessId)
      updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, newAnswer, ZeroEmissionGoodsVehiclePage)
    } yield (updatedUserAnswers, redirectMode)
}
