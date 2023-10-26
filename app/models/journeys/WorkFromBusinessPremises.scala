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

package models.journeys

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WorkFromBusinessPremises

object WorkFromBusinessPremises extends Enumerable.Implicits {

  case object YesAllowable    extends WithName("yesAllowable") with WorkFromBusinessPremises
  case object YesDisallowable extends WithName("yesDisallowable") with WorkFromBusinessPremises
  case object No              extends WithName("no") with WorkFromBusinessPremises

  val values: Seq[WorkFromBusinessPremises] = Seq(
    YesAllowable,
    YesDisallowable,
    No
  )

  def options(authUserType: String)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    val optUserType = if (value.equals(No)) "" else s".$authUserType"
    RadioItem(
      content = Text(messages(s"expenses.${value.toString}$optUserType")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[WorkFromBusinessPremises] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
