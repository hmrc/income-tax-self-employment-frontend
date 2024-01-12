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

package utils

import base.SpecBase.withClue
import org.scalatest.Assertion
import org.scalatest.Assertions._

object Assertions {

  /** Use this assertion if two strings are large, it make investigation what is wrong much easier
    */
  def assertEqualWithDiff(actual: String, expected: String): Assertion = {
    val actualLines   = actual.split("\n")
    val expectedLines = expected.split("\n")

    val diff = actualLines
      .zip(expectedLines)
      .collect {
        case (lhs, rhs) if lhs != rhs =>
          s"== actual and expected lines are not the same ==\n$lhs\n$rhs\n"
      }

    withClue(s"START diff\n(1st line: actual, 2nd line: expected):\n${diff.mkString("\n")}\nEND diff\n") {
      assert(actual === expected)
    }
  }
}
