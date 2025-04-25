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

package builders

import base.SpecBase.{businessId, taxYear}
import controllers.journeys.expenses.travelAndAccommodation.routes
import models.{CheckMode, Index}
import viewmodels.components.{OneColumnSummaryAction, OneColumnSummaryRow}

object OneColumnSummaryBuilder {

  val index = Index(1)

  val testVehicles: List[OneColumnSummaryRow] = List(
//    OneColumnSummaryRow(
//      messageKey = "Vehicle 1",
//      actions = List(
//        OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, CheckMode).url),
//        OneColumnSummaryAction("site.remove", routes.RemoveVehicleController.onPageLoad(taxYear, businessId, CheckMode).url)
//      )
//    ),
//    OneColumnSummaryRow(
//      messageKey = "Vehicle 2",
//      actions = List(
//        OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, CheckMode).url),
//        OneColumnSummaryAction("site.remove", routes.RemoveVehicleController.onPageLoad(taxYear, businessId, CheckMode).url)
//      )
//    )
//  )

    OneColumnSummaryRow(
      messageKey = "Vehicle 1",
      actions = List(
        OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, index, CheckMode).url),
        OneColumnSummaryAction("site.remove", routes.RemoveVehicleController.onPageLoad(taxYear, businessId, CheckMode).url)
      )
    ),
    OneColumnSummaryRow(
      messageKey = "Vehicle 2",
      actions = List(
        OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, index, CheckMode).url),
        OneColumnSummaryAction("site.remove", routes.RemoveVehicleController.onPageLoad(taxYear, businessId, CheckMode).url)
      )
    )
  )

  val testVehicle: List[OneColumnSummaryRow] = List(
    OneColumnSummaryRow(
      messageKey = "Vehicle 1",
      actions = List(
        OneColumnSummaryAction("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, index, CheckMode).url)
      )
    )
  )

}
