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

package pages

import base.SpecBase
import base.SpecBase.buildUserAnswers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}

abstract class PageSpecBase(page: OneQuestionPage[_]) extends TableDrivenPropertyChecks with AnyWordSpecLike {
  val hasAllFurtherAnswersCases: TableFor2[JsObject, Boolean]

  "hasAllFurtherAnswers" should {
    "return correct value" in forAll(hasAllFurtherAnswersCases) { case (userAnswers, expected) =>
      assert(page.hasAllFurtherAnswers(SpecBase.businessId, buildUserAnswers(userAnswers)) === expected)
    }
  }
}
