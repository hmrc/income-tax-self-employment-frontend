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

package controllers.journeys.expenses

import base.SpecBase
import models.common.{JourneyAnswersContext, Mtditid}
import models.database.UserAnswers
import models.journeys.Journey.Income
import models.journeys.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsObject, Json}

class ExpensesSpec extends SpecBase {

  private val ctx = JourneyAnswersContext(taxYear, businessId, Mtditid(mtditid), Income)

  private val validData = Json
    .parse(s"""
              |{
              |  "$businessId": {
              |    "officeSupplies": "yesDisallowable",
              |    "officeSuppliesAmount": 100.00,
              |    "officeSuppliesDisallowableAmount": 100.00
              |  }
              |}
              |""".stripMargin)
    .as[JsObject]

  private val validUserAnswers = UserAnswers("id", validData)

  private val invalidData = Json
    .parse(s"""
              |{
              |  "$businessId": {
              |    "officeSupplies": "yesAllowable",
              |    "officeSuppliesAmount": 100.00,
              |    "officeSuppliesDisallowableAmount": 100.00
              |  }
              |}
              |""".stripMargin)
    .as[JsObject]

  private val invalidUserAnswers = UserAnswers("id", invalidData)

  "invalid state exists" - {
    "remove it from the user answer data" in {
      val expectedResult = Json
        .parse(s"""
                    |{
                    |  "$businessId": {
                    |    "officeSupplies": "yesAllowable",
                    |    "officeSuppliesAmount": 100.00
                    |  }
                    |}
                    |""".stripMargin)
        .as[JsObject]

      eliminateInvalidAnswersState[OfficeSuppliesJourneyAnswers](invalidUserAnswers, ctx).data shouldBe expectedResult
    }
  }
  "state is valid" - {
    "leave user answer data unchanged" in {
      eliminateInvalidAnswersState[OfficeSuppliesJourneyAnswers](validUserAnswers, ctx).data shouldBe validData
    }
  }
}
