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

package pages.expenses.tailoring.individualCategories

import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OfficeSupplies
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.{OneQuestionPage, PageJourney}
import queries.Settable

case object OfficeSuppliesPage extends OneQuestionPage[OfficeSupplies] {
  override def toString: String = "officeSupplies"

  override val dependentPagesWhenNo: List[Settable[_]] = List(OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage)

  override def next(userAnswers: UserAnswers, businessId: BusinessId): Option[PageJourney] =
    userAnswers.get(this, businessId).map { _ =>
      PageJourney.mkQuestion(GoodsToSellOrUsePage)
    }
}
