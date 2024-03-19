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

import base.SpecBase.{businessId, emptyUserAnswers, taxYear}
import controllers.StandardControllerSpec
import models.NormalMode

class WritingDownAllowanceControllerSpec extends StandardControllerSpec {
  lazy val getOnPageLoadNormal: String = routes.WritingDownAllowanceController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val postOnSubmitNormal: String  = routes.WritingDownAllowanceController.onSubmit(taxYear, businessId, NormalMode).url
  override def onwardUrl: String       = routes.WdaSpecialRateController.onPageLoad(taxYear, businessId, NormalMode).url

  checkOnPageLoad(getOnPageLoadNormal, emptyUserAnswers, "‘Writing down’ allowance")
  checkOnSubmit(postOnSubmitNormal, emptyUserAnswers)
}
