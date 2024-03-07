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
