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

package controllers.journeys.expenses.travelAndAccommodation

import controllers.actions._
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.UseSimplifiedExpensesPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.UseSimplifiedExpensesView

import javax.inject.Inject

class UseSimplifiedExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    navigator: TravelAndAccommodationNavigator,
    val controllerComponents: MessagesControllerComponents,
    view: UseSimplifiedExpensesView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val redirectRoute = navigator.nextPage(UseSimplifiedExpensesPage, NormalMode, request.userAnswers, taxYear, businessId).url
      getVehicleNameAndLoadPage(businessId) { vehicleName =>
        Ok(view(request.userType, vehicleName, redirectRoute))
      }
    }
}
