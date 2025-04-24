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

package controllers.journeys.expenses

import models.common.BusinessId
import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import models.requests.DataRequest
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.Redirect

package object travelAndAccommodation {

  def getVehicleNameAndLoadPage(businessId: BusinessId)(view: String => Result)(implicit request: DataRequest[AnyContent]): Result =
    request.userAnswers.get(TravelForWorkYourVehiclePage, businessId) match {
      case Some(vehicle) =>
        view(vehicle)
      case None =>
        Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }

  def getVehicleNameAndLoadPage(vehicleDetails: Option[VehicleDetailsDb])(view: String => Result): Result =
    vehicleDetails.flatMap(_.description) match {
      case Some(vehicle) =>
        view(vehicle)
      case None =>
        Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad())
    }

  def stripTrailingZeros(value: BigDecimal): String =
    value.bigDecimal.stripTrailingZeros().toPlainString
}
