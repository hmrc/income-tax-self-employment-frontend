package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import controllers.journeys.clearDependentPages
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

  private[zeroEmissionGoodsVehicle] def submitAnswerAndClearWhenNo(pageUpdated: ZegvBasePage[Boolean],
                                                                   businessId: BusinessId,
                                                                   request: DataRequest[_],
                                                                   newAnswer: Boolean): Future[UserAnswers] =
    for {
      editedUserAnswers  <- clearDependentPages(pageUpdated, request, businessId)
      updatedUserAnswers <- service.persistAnswer(businessId, editedUserAnswers, newAnswer, pageUpdated)
    } yield updatedUserAnswers

  def submitAnswerAndRedirect(pageUpdated: ZegvBasePage[Boolean],
                              businessId: BusinessId,
                              request: DataRequest[_],
                              newAnswer: Boolean,
                              mode: Mode,
                              taxYear: TaxYear): Future[Result] =
    submitAnswerAndClearWhenNo(pageUpdated, businessId, request, newAnswer)
      .map { updatedAnswers =>
        pageUpdated.redirectNext(mode, updatedAnswers, businessId, taxYear)
      }

}
