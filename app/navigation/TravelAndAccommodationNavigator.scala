/*
 * Copyright 2025 HM Revenue & Customs
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

package navigation

import controllers.standard
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.{CheckMode, Mode, NormalMode}
import pages.Page
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class TravelAndAccommodationNavigator @Inject() {
  private val normalRoutes: Page => UserAnswers => TaxYear => BusinessId => Call = { case _ =>
    _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => TaxYear => BusinessId => Call = { case _ =>
    _ => _ => _ => standard.routes.JourneyRecoveryController.onPageLoad()

  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode => normalRoutes(page)(userAnswers)(taxYear)(businessId)
      case CheckMode  => checkRouteMap(page)(userAnswers)(taxYear)(businessId)
    }
}
