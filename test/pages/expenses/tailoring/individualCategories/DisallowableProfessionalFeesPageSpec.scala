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

package pages.expenses.tailoring.individualCategories

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.expenses.professionalFees.{ProfessionalFeesAmountPage, ProfessionalFeesDisallowableAmountPage}

class DisallowableProfessionalFeesPageSpec extends PlaySpec with MockitoSugar {

  "DisallowableProfessionalFeesPage" should {

    "return the correct string" in {
      DisallowableProfessionalFeesPage.toString mustBe "disallowableProfessionalFees"
    }

    "return the correct pages when no" in {
      DisallowableProfessionalFeesPage.dependentPagesWhenNo mustBe List(ProfessionalFeesDisallowableAmountPage, ProfessionalFeesAmountPage)
    }

  }
}
