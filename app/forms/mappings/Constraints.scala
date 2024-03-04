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

import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.math.Ordered.orderingToOrdered

trait Constraints {

  private def check[A](predicate: A => Boolean, onError: => Invalid): Constraint[A] =
    Constraint { input =>
      if (predicate(input))
        Valid
      else
        onError
    }

  def minimumValue[A: Ordering](minimum: A, errorKey: String): Constraint[A] =
    check((a: A) => a >= minimum, Invalid(errorKey, minimum))

  def maximumValue[A: Ordering](maximum: A, errorKey: String): Constraint[A] =
    check((a: A) => a <= maximum, Invalid(errorKey, maximum))

  def lessThanOrEqualTo[A: Ordering](value: A, errorKey: String, errorArgument: Option[String] = None): Constraint[A] =
    check((a: A) => a <= value, Invalid(errorKey, errorArgument.getOrElse(value)))

  def greaterThan[A: Ordering](value: A, errorKey: String, errorArgument: Option[String] = None): Constraint[A] =
    check((a: A) => a > value, Invalid(errorKey, errorArgument.getOrElse(value)))

  def lessThan[A: Ordering](value: A, errorKey: String, errorArgument: Option[String] = None): Constraint[A] =
    check((a: A) => a < value, Invalid(errorKey, errorArgument.getOrElse(value)))

  def inRange[A: Ordering](minimum: A, maximum: A, errorKey: String): Constraint[A] =
    check((a: A) => a >= minimum && a <= maximum, Invalid(errorKey, minimum, maximum))

  def regexp(regexp: String, errorKey: String): Constraint[String] =
    check((a: String) => a.matches(regexp), Invalid(errorKey, regexp))

  def regexpBigDecimal(regex: String, errorKey: String): Constraint[BigDecimal] =
    check((a: BigDecimal) => a.toString.stripSuffix(".00").matches(regex), Invalid(errorKey, regex))

  def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    check((a: String) => a.length <= maximum, Invalid(errorKey, maximum))

  def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint { set =>
      if (set.nonEmpty)
        Valid
      else
        Invalid(errorKey)
    }

}

object Constraints extends Constraints
