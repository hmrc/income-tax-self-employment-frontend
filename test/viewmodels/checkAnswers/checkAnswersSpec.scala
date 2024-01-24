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

import base.SpecBase.{TestCase, emptyCall}
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.govukfrontend.views.Aliases._

class checkAnswersSpec extends AnyWordSpecLike {
  private def expectedRow(expectedAnswer: String) = SummaryListRow(
    Key(Text("keyMessage"), "govuk-summary-list__key govuk-!-width-one-third"),
    Value(HtmlContent(expectedAnswer), "govuk-summary-list__value govuk-!-width-two-thirds"),
    "",
    Some(Actions("", List(ActionItem("", Text("Change"), Some("changeMessage"), "", Map()))))
  )

  "buildRowBoolean" should {

    "return a SummaryListRow with Yes for answer=True" in new TestCase {
      val result = buildRowBoolean(answer = true, emptyCall, "keyMessage", "changeMessage")
      assert(result === expectedRow("Yes"))
    }

    "return a SummaryListRow with No for answer=False" in new TestCase {
      val result = buildRowBoolean(answer = false, emptyCall, "keyMessage", "changeMessage")
      assert(result === expectedRow("No"))
    }
  }

  "buildRowBigDecimal" should {
    "return a SummaryListRow with formatted money" in new TestCase {
      val result = buildRowBigDecimal(1.0, emptyCall, "keyMessage", "changeMessage")
      assert(result === expectedRow("£1.00"))
    }
  }

  "buildRowString" should {
    "return a SummaryListRow with a proper key, value and actions" in new TestCase {
      val result = buildRowString("strAnswer", emptyCall, "keyMessage", "changeMessage")
      assert(result === expectedRow("strAnswer"))
    }
  }
}
