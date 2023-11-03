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

package pages

import models.journeys.expenses.DisallowableSubcontractorCosts
import play.api.libs.json.JsPath

case object DisallowableSubcontractorCostsPage extends QuestionPage[DisallowableSubcontractorCosts] {

  override def path(businessId: Option[String] = None): JsPath =
    if (businessId.isEmpty) JsPath \ toString else JsPath \ businessId.get \ toString

  override def toString: String = "disallowableSubcontractorCosts"
}
