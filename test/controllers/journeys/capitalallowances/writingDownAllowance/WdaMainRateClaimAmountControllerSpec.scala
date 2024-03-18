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

package controllers.journeys.capitalallowances.writingDownAllowance

import base.SpecBase.{buildUserAnswers, businessId, emptyUserAnswers, taxYear}
import controllers.StandardControllerSpec
import models.NormalMode
import play.api.libs.json.Json

class WdaMainRateClaimAmountControllerSpec extends StandardControllerSpec {
  lazy val getOnPageLoadNormal: String = routes.WdaMainRateClaimAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val postOnSubmitNormal: String  = routes.WdaMainRateClaimAmountController.onSubmit(taxYear, businessId, NormalMode).url

  checkOnPageLoad(getOnPageLoadNormal, emptyUserAnswers, "Claim amount")
  checkOnSubmit(
    postOnSubmitNormal,
    buildUserAnswers(
      Json.obj("wdaMainRate" -> "true")
    ),
    ("value", "10"))
}
