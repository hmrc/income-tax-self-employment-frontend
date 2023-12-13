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

package controllers.journeys

import models.common.JourneyContext
import models.database.UserAnswers
import models.journeys.expenses.ExpensesJourneyAnswers
import play.api.libs.json.{JsObject, Reads}

package object expenses {
  private[expenses] def eliminateInvalidAnswersState[A <: ExpensesJourneyAnswers: Reads](answers: UserAnswers, ctx: JourneyContext): UserAnswers = {
    val answersData    = (answers.data \ ctx.businessId.value).as[JsObject]
    val journeyAnswers = answersData.as[A]

    journeyAnswers.disallowableAmount.fold(answers) { _ =>
      (answersData \ journeyAnswers.correspondingTailoringPageName.value).as[String] match {
        case "yesAllowable" =>
          val validAnswers = answersData - journeyAnswers.disallowablePageName.value
          val validData    = answers.data + (ctx.businessId.value -> validAnswers)
          answers.copy(data = validData)

        case _ => answers
      }
    }
  }
}
