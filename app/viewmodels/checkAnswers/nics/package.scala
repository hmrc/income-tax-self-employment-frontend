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

package viewmodels.checkAnswers

import models.common.BusinessId
import models.domain.BusinessData
import play.api.i18n.Messages

package object nics {

  // TODO these will be used/changed when the CYA content is added for multiple businesses

  private val differentReasonAnswer = BusinessId("I’m exempt for a different reason")

  private def getTradingNameFromId(id: BusinessId, businesses: Seq[BusinessData]): String =
    businesses.find(_.businessId == id.value).flatMap(business => business.tradingName).getOrElse(id.value)

  def formatBusinessNamesAnswers(answers: List[BusinessId], businesses: Seq[BusinessData])(implicit messages: Messages): String =
    if (answers.contains(differentReasonAnswer)) messages("nics.exemptForDifferentReason")
    else answers.map(id => getTradingNameFromId(id, businesses)).mkString(",<br>")
}
