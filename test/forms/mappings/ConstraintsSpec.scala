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

package forms.mappings

import forms.mappings.Constraints._
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.validation.{Invalid, Valid}

class ConstraintsSpec extends AnyWordSpecLike {

  "Constraints" should {
    "validate minimumValue correctly" in {
      assert(minimumValue(1, "error.min").apply(2) === Valid)
      assert(minimumValue(1, "error.min").apply(1) === Valid)
      assert(minimumValue(1, "error.min").apply(0) === Invalid("error.min", 1))
    }

    "validate maximumValue correctly" in {
      assert(maximumValue(1, "error.max").apply(0) === Valid)
      assert(maximumValue(1, "error.max").apply(1) === Valid)
      assert(maximumValue(1, "error.max").apply(2) === Invalid("error.max", 1))
    }

    "validate greaterThan correctly" in {
      assert(greaterThan(0, "error.greaterThan").apply(1) === Valid)
      assert(greaterThan(0, "error.greaterThan").apply(0) === Invalid("error.greaterThan", 0))
      assert(greaterThan(0, "error.greaterThan").apply(-1) === Invalid("error.greaterThan", 0))
    }

    "validate lessThan correctly" in {
      assert(lessThan(0, "error.lessThan").apply(-1) === Valid)
      assert(lessThan(0, "error.lessThan").apply(0) === Invalid("error.lessThan", 0))
      assert(lessThan(0, "error.lessThan").apply(1) === Invalid("error.lessThan", 0))
    }

    "validate inRange correctly" in {
      assert(inRange(1, 3, "error.inRange").apply(2) === Valid)
      assert(inRange(1, 3, "error.inRange").apply(4) === Invalid("error.inRange", 1, 3))
    }

    "validate regexp correctly" in {
      assert(regexp("""^\w+$""", "error.invalid").apply("foo") === Valid)
      assert(regexp("""^\d+$""", "error.invalid").apply("foo") === Invalid("error.invalid", """^\d+$"""))
    }

    "validate regexpBigDecimal correctly" in {
      assert(regexpBigDecimal("""^\d+\.\d{2}$""", "error.regexpBigDecimal").apply(BigDecimal("123.45")) === Valid)
      assert(
        regexpBigDecimal("""^\d+\.\d{2}$""", "error.regexpBigDecimal")
          .apply(BigDecimal("123")) === Invalid("error.regexpBigDecimal", """^\d+\.\d{2}$"""))
    }

    "validate maxLength correctly" in {
      assert(maxLength(10, "error.length").apply("a" * 9) === Valid)
      assert(maxLength(10, "error.length").apply("") === Valid)
      assert(maxLength(10, "error.length").apply("a" * 10) === Valid)
      assert(maxLength(10, "error.length").apply("a" * 11) === Invalid("error.length", 10))
    }

    "validate nonEmptySet correctly" in {
      assert(nonEmptySet("error.nonEmptySet").apply(Set(1, 2, 3)) === Valid)
      assert(nonEmptySet("error.nonEmptySet").apply(Set.empty) === Invalid("error.nonEmptySet"))
    }
  }

}
