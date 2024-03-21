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

import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import pages.{Page, QuestionPage}
import play.api.libs.json.Reads

case class PageHopping(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear) {

  def canHopTo[A: Reads](from: QuestionPage[A], to: QuestionPage[_]): Boolean =
    true

  // from: ExpensesCategoriesPage to OfficeSuppliesPage
}
