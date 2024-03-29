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

package pages.capitalallowances.zeroEmissionGoodsVehicle

import controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle.routes
import controllers.standard
import models.NormalMode
import models.common._
import models.database.UserAnswers
import pages.redirectOnBoolean
import play.api.mvc.Call
import queries.Settable

object ZegvOnlyForSelfEmploymentPage extends ZegvBasePage[Boolean] {
  override def toString: String = "zegvOnlyForSelfEmployment"

  override val dependentPagesWhenYes: List[Settable[_]] =
    List(
      ZegvUseOutsideSEPage,
      ZegvUseOutsideSEPercentagePage
    )

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    userAnswers.get(ZegvOnlyForSelfEmploymentPage, Some(businessId)).fold(standard.routes.JourneyRecoveryController.onPageLoad()) { _ =>
      redirectOnBoolean(
        this,
        userAnswers,
        businessId,
        onTrue = routes.ZegvHowMuchDoYouWantToClaimController.onPageLoad(taxYear, businessId, NormalMode),
        onFalse = routes.ZegvUseOutsideSEController.onPageLoad(taxYear, businessId, NormalMode)
      )
    }

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean = {
    val answer = userAnswers.get(this, businessId)
    (answer.contains(true) && ZegvHowMuchDoYouWantToClaimPage.hasAllFurtherAnswers(businessId, userAnswers)) ||
    (answer.contains(false) && ZegvUseOutsideSEPage.hasAllFurtherAnswers(businessId, userAnswers))
  }

}
