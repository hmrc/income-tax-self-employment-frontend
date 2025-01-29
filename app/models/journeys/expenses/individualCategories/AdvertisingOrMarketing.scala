/*
 * Copyright 2025 HM Revenue & Customs
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

package models.journeys.expenses.individualCategories

import models.common.{Enumerable, UserType, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait AdvertisingOrMarketing

object AdvertisingOrMarketing extends Enumerable.Implicits {

  case object YesAllowable    extends WithName("yesAllowable") with AdvertisingOrMarketing
  case object YesDisallowable extends WithName("yesDisallowable") with AdvertisingOrMarketing
  case object No              extends WithName("no") with AdvertisingOrMarketing

  val values: Seq[AdvertisingOrMarketing] = Seq(
    YesAllowable,
    YesDisallowable,
    No
  )

  def options(userType: UserType)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    val optUserType = if (value.equals(No)) "" else s".$userType"
    RadioItem(
      content = Text(messages(if (value == No) "site.no" else s"expenses.${value.toString}$optUserType")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[AdvertisingOrMarketing] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
