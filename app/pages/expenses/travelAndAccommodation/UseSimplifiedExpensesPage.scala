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

package pages.expenses.travelAndAccommodation

import models.Index
import models.common.{BusinessId, TaxYear}
import pages.Page
import play.api.mvc.Call

case object UseSimplifiedExpensesPage extends Page {
  def route(taxYear: TaxYear, businessId: BusinessId, index: Index): Call =
    controllers.journeys.expenses.travelAndAccommodation.routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId, index)

}
