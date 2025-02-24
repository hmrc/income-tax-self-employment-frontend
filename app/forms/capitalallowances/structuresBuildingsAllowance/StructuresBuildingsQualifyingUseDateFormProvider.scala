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

package forms.capitalallowances.structuresBuildingsAllowance

import forms.mappings.Mappings
import models.common.UserType
import play.api.data.Form
import utils.TimeMachine

import java.time.LocalDate
import javax.inject.Inject

class StructuresBuildingsQualifyingUseDateFormProvider @Inject() (timeMachine: TimeMachine) extends Mappings {

  private val latestDate    = timeMachine.now
  private val requiredError = "structuresBuildingsQualifyingUseDate.error"
  private val dateInFuture  = "structuresBuildingsQualifyingUseDate.error.inFuture"

  def apply(userType: UserType): Form[LocalDate] =
    Form(
      "structuresBuildingsQualifyingUseDate" -> localDate(
        requiredKey = s"$requiredError.$userType",
        latestDateAndError = Some((latestDate, dateInFuture))
      )
    )
}
