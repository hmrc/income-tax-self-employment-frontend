/*
 * Copyright 2023 HM Revenue & Customs
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

import pages.OneQuestionPage
import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OfficeSupplies
import pages.{OneQuestionPage, QuestionPage}


case object DisallowableProfessionalFeesPage extends OneQuestionPage[Boolean] {
  override def toString: String = "disallowableProfessionalFees"

  override def next(userAnswers: UserAnswers, businessId: BusinessId): Option[QuestionPage[_]] =
    userAnswers.get(this, businessId).map { _ =>
      GoodsToSellOrUsePage
    }

}
