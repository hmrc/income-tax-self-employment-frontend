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

package builders

import controllers.actions.AuthenticatedIdentifierAction.User
import uk.gov.hmrc.auth.core.AffinityGroup

object UserBuilder {
  val aNoddyUser = User("mtdItId", arn = None, "nino", AffinityGroup.Individual.toString)
  val aNoddyAgentUser = User("mtdItId", arn = Some(""), "nino", AffinityGroup.Agent.toString)
}
