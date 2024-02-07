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

package models.common

import enumeratum._
import play.api.mvc.PathBindable

sealed trait AccountingType extends EnumEntry {
  override def entryName: String = toString.toUpperCase
}

object AccountingType extends Enumerable.Implicits {
  val values: Seq[AccountingType] = Seq(Accrual, Cash)

  case object Accrual extends WithName("ACCRUAL") with AccountingType
  case object Cash    extends WithName("CASH") with AccountingType

  implicit def pathBindable(implicit strBinder: PathBindable[String]): PathBindable[AccountingType] = new PathBindable[AccountingType] {

    override def bind(key: String, value: String): Either[String, AccountingType] =
      values.find(_.toString.equalsIgnoreCase(value)).toRight(s"Invalid accounting type with key $key and value $value")

    override def unbind(key: String, accountingType: AccountingType): String =
      strBinder.unbind(key, accountingType.entryName)
  }

  implicit val enumerable: Enumerable[AccountingType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
