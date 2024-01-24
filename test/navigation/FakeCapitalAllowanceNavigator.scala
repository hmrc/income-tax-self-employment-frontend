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

package navigation

import models.Mode
import models.common.{BusinessId, TaxYear, onwardRoute}
import models.database.UserAnswers
import pages.Page
import play.api.mvc.Call

class FakeCapitalAllowanceNavigator(desiredRoute: Call) extends CapitalAllowancesNavigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    desiredRoute

}

object FakeCapitalAllowanceNavigator {
  def apply(): FakeCapitalAllowanceNavigator = new FakeCapitalAllowanceNavigator(onwardRoute)
}
