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

package config

/** Put tax year speficic values here. This file will have to be updated every year for the new tax year requirements.
  *
  * IMPORTANT: It is essential to understand how many tax year we are supporting. And providing different values depends on the tax year
  */
object TaxYearConfig {

  /** if turnover > incomeThreshold, then the user must use catagories for expeness */
  val incomeThreshold: BigDecimal = 85000
}
