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

import base.SpecBase
import base.SpecBase.applicationBuilder
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.checkAnswers.checkAnswersSpec.TestCase

class checkAnswersSpec extends AnyWordSpecLike {

  "buildRowBoolean" should {
    def expectedRow(expectedAnswer: String) = SummaryListRow(
      Key(Text("keyMessage"), "govuk-!-width-two-thirds"),
      Value(HtmlContent(expectedAnswer), "govuk-!-width-one-third"),
      "",
      Some(Actions("", List(ActionItem("", Text("Change"), Some("changeMessage"), "", Map()))))
    )

    "return a SummaryListRow with Yes for answer=True" in new TestCase {
      running(application) {
        val result = buildRowBoolean(answer = true, Call("", "", ""), "keyMessage", "changeMessage")
        assert(result === expectedRow("Yes"))
      }
    }

    "return a SummaryListRow with No for answer=False" in new TestCase {
      running(application) {
        val result = buildRowBoolean(answer = false, Call("", "", ""), "keyMessage", "changeMessage")
        assert(result === expectedRow("No"))
      }
    }

  }
}

object checkAnswersSpec {
  trait TestCase {
    val application            = applicationBuilder(userAnswers = None).build()
    implicit val msg: Messages = SpecBase.messages(application)
  }
}
