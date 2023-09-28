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

package models.requests

import play.api.libs.json.{Json, OFormat}

case class TaggedTradeDetails(businessId: String,
                              tradingName: Option[String],
                              abroadStatus: String,
                              incomeStatus: String,
                              expensesStatus: String,
                              nationalInsuranceStatus: String)

object TaggedTradeDetails {

  implicit val format: OFormat[TaggedTradeDetails] = Json.format[TaggedTradeDetails]

  private val completedStatus = "completed"
  private val inProgressStatus = "inProgress"
  private val notStartedStatus = "notStarted"
  private val cannotStartYetStatus = "cannotStartYet"

  def addCannotStartStatus(data: TaggedTradeDetails): TaggedTradeDetails = {
    val income = if (data.abroadStatus.equals(notStartedStatus)) cannotStartYetStatus else data.incomeStatus
    val expenses = if (data.incomeStatus.equals(notStartedStatus)) cannotStartYetStatus else data.expensesStatus
    val nationalInsurance = if (data.expensesStatus.equals(notStartedStatus)) cannotStartYetStatus else data.nationalInsuranceStatus
    data.copy(incomeStatus = income, expensesStatus = expenses, nationalInsuranceStatus = nationalInsurance)
  }
}
