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

package forms.adjustments.profitOrLoss

import forms.mappings.Mappings
import models.common.UserType
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss
import pages.OneQuestionPage
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class WhatDoYouWantToDoWithLossFormProvider @Inject() extends Mappings {

  def apply(page: OneQuestionPage[_], userType: UserType): Form[Set[WhatDoYouWantToDoWithLoss]] =
    Form(
      "value" -> set(enumerable[WhatDoYouWantToDoWithLoss](s"${page.pageName}.error.required.$userType"))
        .verifying(nonEmptySet(s"${page.pageName}.error.required.$userType")))
}
