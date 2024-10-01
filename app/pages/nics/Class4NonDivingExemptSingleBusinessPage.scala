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

package pages.nics

import models.common.BusinessId.nationalInsuranceContributions
import models.common.{Business, BusinessId, TaxYear}
import models.database.UserAnswers
import play.api.mvc.Call

case object Class4NonDivingExemptSingleBusinessPage extends NicsBasePage[List[BusinessId]] {
  override def toString: String = "class4NonDivingExemptSingleBusiness"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    cyaPage(taxYear, BusinessId.nationalInsuranceContributions)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined

  def remainingBusinesses(userAnswers: UserAnswers): List[Business] = {
    val previouslySelected = userAnswers.get(Class4DivingExemptPage, nationalInsuranceContributions).getOrElse(List.empty)

    userAnswers.getBusinesses.filterNot(business => previouslySelected.contains(business.businessId))
  }
}
