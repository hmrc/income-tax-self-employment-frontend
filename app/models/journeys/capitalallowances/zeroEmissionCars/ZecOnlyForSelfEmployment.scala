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

package models.journeys.capitalallowances.zeroEmissionCars

import models.common.{Enumerable, UserType, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ZecOnlyForSelfEmployment
object ZecOnlyForSelfEmployment extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with ZecOnlyForSelfEmployment

  case object No extends WithName("no") with ZecOnlyForSelfEmployment

  val values: Seq[ZecOnlyForSelfEmployment] = Seq(
    Yes,
    No
  )

  def options(userType: UserType)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    val optUserType = if (value.equals(Yes)) "" else s".$userType"
    RadioItem(
      content = Text(messages(if (value == Yes) "site.yes" else s"zecUsedForSelfEmployment.${value.toString}$optUserType")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[ZecOnlyForSelfEmployment] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
