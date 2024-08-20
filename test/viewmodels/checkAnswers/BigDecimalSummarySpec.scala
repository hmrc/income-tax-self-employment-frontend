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
import models.common.UserType
import org.scalatest.wordspec.AnyWordSpecLike
import pages.OneQuestionPage
import play.api.libs.json.Json

class BigDecimalSummarySpec extends AnyWordSpecLike {
  object TestPage extends OneQuestionPage[BigDecimal] {
    override def toString: String = "someBigDecimalPage"
  }

  "row" should {
    UserType.values.foreach { userType =>
      s"return a SummaryListRow for $userType" in {
        val summary = new BigDecimalSummary(TestPage, call)
        val answers = buildUserAnswers(
          Json.obj(TestPage.pageName.value -> "12344321.0")
        )

        val result = summary.row(answers, taxYear, businessId, userType)(messagesStubbed).value
        assert(result.key.content.asHtml.toString() === s"someBigDecimalPage.subHeading.cya.$userType")
        assert(result.value.content.asHtml.toString() === "£12,344,321")
      }
    }
  }
}
