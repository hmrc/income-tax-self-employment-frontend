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

package pages.capitalallowances.specialTaxSites

import base.SpecBase
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite.newSite

class SpecialTaxSitesBasePageSpec extends SpecBase {
//  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean]

  "isNewSite" - {
//    "should return true" in {
//      isNewSite(fakeDataRequest(emptyUserAnswersAccrual), businessId, 0)
//      isNewSite(fakeDataRequest(buildUserAnswers()))
    "should add new to an empty list" in {
      val list: List[NewSpecialTaxSite] = List()
      list.updated(0, newSite) == List(newSite)
    }
    "should update element one" in {
      val list: List[NewSpecialTaxSite] = List(NewSpecialTaxSite(Some(true)))
      list.updated(0, NewSpecialTaxSite(Some(false))) == List(NewSpecialTaxSite(Some(false)))
    }
    "should add new to an existing list" in {
      val list: List[NewSpecialTaxSite] = List(NewSpecialTaxSite(Some(true)))
      list.updated(1, NewSpecialTaxSite(Some(false))) == List(NewSpecialTaxSite(Some(true)), NewSpecialTaxSite(Some(false)))
    }
  }
}
