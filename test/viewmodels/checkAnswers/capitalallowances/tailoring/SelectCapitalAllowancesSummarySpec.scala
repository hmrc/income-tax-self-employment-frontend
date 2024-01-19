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

package viewmodels.checkAnswers.capitalallowances.tailoring

import base.SpecBase
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class SelectCapitalAllowancesSummarySpec extends SpecBase {

  def expectedResult(answers: UserAnswers): Option[SummaryListRow] =
    SelectCapitalAllowancesSummary.row(answers, taxYear, businessId)(messagesStubbed)

  "no answers exist" - {
    "return None" in {
      expectedResult(emptyUserAnswers) shouldBe None
    }
  }
  "ClaimCapitalAllowance answers" - {
    "are false" - {
      "return None" in {
        val data = Json.obj("claimCapitalAllowances" -> false)

        expectedResult(buildUserAnswers(data)) shouldBe None
      }
    }
    "are true" - {
      "allowances are empty" - {
        "return site.none" in {
          val data = Json.obj("claimCapitalAllowances" -> true, "selectCapitalAllowances" -> JsArray.empty)

          expectedResult(buildUserAnswers(data)).get shouldBe a[SummaryListRow]
          expectedResult(buildUserAnswers(data)).get.value.content shouldBe HtmlContent("site.none")
        }
      }
      "allowances exist" - {
        "return allowances as content" in {
          val data = Json.obj("claimCapitalAllowances" -> true, "selectCapitalAllowances" -> Json.arr("zeroEmissionCar"))

          expectedResult(buildUserAnswers(data)).get shouldBe a[SummaryListRow]
          expectedResult(buildUserAnswers(data)).get.value.content shouldBe HtmlContent("selectCapitalAllowances.zeroEmissionCar")
        }
      }
    }
  }

}
