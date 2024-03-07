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

  private[zeroEmissionGoodsVehicle] def submitAnswerAndClearDependentAnswers(pageUpdated: ZegvBasePage[Boolean],
                                                                             businessId: BusinessId,
                                                                             request: DataRequest[_],
                                                                             newAnswer: Boolean): Future[UserAnswers] =
    for {
      editedUserAnswers  <- clearDependentPages(pageUpdated, newAnswer, request, businessId)
      updatedUserAnswers <- service.persistAnswer(businessId, editedUserAnswers, newAnswer, pageUpdated)
    } yield updatedUserAnswers

  def submitAnswerAndRedirect(pageUpdated: ZegvBasePage[Boolean],
                              businessId: BusinessId,
                              request: DataRequest[_],
                              newAnswer: Boolean,
                              taxYear: TaxYear,
                              mode: Mode): Future[Result] =
    submitAnswerAndClearDependentAnswers(pageUpdated, businessId, request, newAnswer)
      .map { updatedAnswers =>
        pageUpdated.redirectNext(mode, updatedAnswers, businessId, taxYear)
      }

}
