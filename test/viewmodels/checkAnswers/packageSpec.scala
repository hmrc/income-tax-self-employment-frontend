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

import base.SpecBase._
import models.common.UserType.Individual
import org.scalatest.wordspec.AnyWordSpecLike
import pages.Page

import java.time.LocalDate

class packageSpec extends AnyWordSpecLike {
  object StubPage extends Page {
    override def toString: String = "stubPage"
  }

  "mkBooleanSummary" should {
    List(true, false).foreach { answer =>
      s"build boolean summary row for answer=$answer" in {
        val result = mkBooleanSummary(answer = answer, call, StubPage, Individual, true)(messagesStubbed)
        assert(result.key.content.asHtml.toString() === "stubPage.subHeading.cya.individual")
        assert(result.value.content.asHtml.toString() === s"site.${if (answer) "yes" else "no"}")
      }
    }
  }

  "mkBigDecimalSummary" should {
    "build big decimal summary row" in {
      val result = mkBigDecimalSummary(answer = BigDecimal(10.0), call, StubPage, Individual)(messagesStubbed)
      assert(result.key.content.asHtml.toString() === "stubPage.subHeading.cya.individual")
      assert(result.value.content.asHtml.toString() === "£10")
    }
  }

  "formatDate" should {
    "convert LocalDates into the valid 'd MMMM yyyy' format" in {
      val localDates      = Seq(LocalDate.of(2020, 3, 3), LocalDate.of(3020, 5, 30))
      val expectedAnswers = Seq("3 March 2020", "30 May 3020")
      localDates.map(formatDate) mustEqual expectedAnswers
    }
  }
}
