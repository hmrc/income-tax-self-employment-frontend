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

package base

import models.common.UserType.{Agent, Individual}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call

trait CYAControllerBaseSpec extends ControllerSpec {

  protected val userTypes: List[UserType] = List(Individual, Agent)

  protected def onSubmitCall: (TaxYear, BusinessId) => Call
  protected def buildUserAnswers(data: JsObject): UserAnswers = UserAnswers(userAnswersId, Json.obj(businessId.value -> data))

}
