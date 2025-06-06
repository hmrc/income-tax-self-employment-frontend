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

import controllers.journeys.industrysectors.routes
import controllers.standard.{routes => standardRoutes}
import models.common.{BusinessId, TaxYear}
import models.journeys.industrySectors.IndustrySectorsDb
import models.{CheckMode, Mode, NormalMode}
import pages.Page
import pages.industrysectors.{FarmerOrMarketGardenerPage, LiteraryOrCreativeWorksPage}
import play.api.mvc.Call

import javax.inject.Inject

class IndustrySectorsNavigator @Inject() {

  private val normalRoutes: Page => IndustrySectorsDb => (TaxYear, BusinessId) => Call = {
    case FarmerOrMarketGardenerPage =>
      _ => (taxYear, businessId) => routes.LiteraryOrCreativeWorksController.onPageLoad(taxYear, businessId, NormalMode)
    case LiteraryOrCreativeWorksPage =>
      _ => (taxYear, businessId) => routes.IndustrySectorsAndAbroadCYAController.onPageLoad(taxYear, businessId)

    case _ => _ => (_, _) => standardRoutes.JourneyRecoveryController.onPageLoad()
  }

  private val checkRouteMap: Page => IndustrySectorsDb => (TaxYear, BusinessId) => Call = { case _ =>
    _ => (taxYear, businessId) => routes.IndustrySectorsAndAbroadCYAController.onPageLoad(taxYear, businessId)
  }

  def nextPage(page: Page, mode: Mode, model: IndustrySectorsDb, taxYear: TaxYear, businessId: BusinessId): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(model)(taxYear, businessId)
      case CheckMode =>
        checkRouteMap(page)(model)(taxYear, businessId)
    }

}
