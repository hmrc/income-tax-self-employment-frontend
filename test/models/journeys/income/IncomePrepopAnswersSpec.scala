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

package models.journeys.income

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}

class IncomePrepopAnswersSpec extends AnyFreeSpec with TableDrivenPropertyChecks {

  "IncomePrepopAnswers .totalIncome" - {
    "should return the sum of the optional turnoverIncome and otherIncome values, or zero if both are None" in {
      val tests: TableFor2[IncomePrepopAnswers, BigDecimal] = Table(
        ("answers", "expectedSum"),
        (IncomePrepopAnswers(Some(20), Some(10)), 30),
        (IncomePrepopAnswers(None, Some(10)), 10),
        (IncomePrepopAnswers(Some(20), None), 20),
        (IncomePrepopAnswers(None, None), 0)
      )
      forAll(tests) { case (answers, expectedSum) =>
        answers.totalIncome == expectedSum
      }
    }
  }
}
