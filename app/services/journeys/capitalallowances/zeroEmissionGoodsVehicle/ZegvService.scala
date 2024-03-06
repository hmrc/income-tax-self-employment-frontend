package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import controllers.journeys.clearPagesWhenNo
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.requests.DataRequest
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvBasePage
import play.api.mvc.Result
import services.SelfEmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ZegvService @Inject() (service: SelfEmploymentService)(implicit ec: ExecutionContext) {

  private[zeroEmissionGoodsVehicle] def submitAnswer(pageUpdated: ZegvBasePage[Boolean],
                                                     businessId: BusinessId,
                                                     request: DataRequest[_],
                                                     newAnswer: Boolean,
                                                     mode: Mode): Future[(UserAnswers, Mode)] =
    for {
      (editedUserAnswers, redirectMode) <- clearPagesWhenNo(pageUpdated, newAnswer, request, mode, businessId)
      updatedUserAnswers                <- service.persistAnswer(businessId, editedUserAnswers, newAnswer, pageUpdated)
    } yield (updatedUserAnswers, redirectMode)

  def submitAnswerAndRedirect(pageUpdated: ZegvBasePage[Boolean],
                              businessId: BusinessId,
                              request: DataRequest[_],
                              newAnswer: Boolean,
                              mode: Mode,
                              taxYear: TaxYear): Future[Result] =
    submitAnswer(pageUpdated, businessId, request, newAnswer, mode)
      .map { case (updatedAnswers, updatedMode) =>
        pageUpdated.redirectNext(updatedMode, updatedAnswers, businessId, taxYear)
      }

}
