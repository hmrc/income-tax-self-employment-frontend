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

package forms.capitalallowances.zeroEmissionGoodsVehicle

import common.Messages._
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider.standardErrors._
import forms.invalidError
import models.common.MoneyBounds.noDecimalRegexp
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.FormError
import ZegvHowMuchDoYouWantToClaimFormProviderSpec._

class ZegvHowMuchDoYouWantToClaimFormProviderSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {

  "form provider" should {
    val invalid            = List(FormError("howMuchDoYouWantToClaim", invalidError, List()))
    val individualRequired = List(FormError("howMuchDoYouWantToClaim", requiredError(Individual)))
    val agentRequired      = List(FormError("howMuchDoYouWantToClaim", requiredError(Agent)))
    val amountRequired     = List(FormError("totalCost", amountRequiredError, List()))
    val lessThanZero       = List(FormError("totalCost", forms.lessThanZeroError, List(0)))
    val nonNumeric         = List(FormError("totalCost", forms.nonNumericError, List()))
    val noDecimals         = List(FormError("totalCost", forms.noDecimalsError, List(noDecimalRegexp)))
    val overMax            = List(FormError("totalCost", overMaxError, List(1000)))

    // @formatter:off
    val cases = Table(
      ("formProvider", "data", "expectedErrors"),
      (individualFormProvider, Map("howMuchDoYouWantToClaim" -> "fullCost"), Nil),
      (agentFormProvider, Map("howMuchDoYouWantToClaim" -> "fullCost"), Nil),
      (individualFormProvider, Map("howMuchDoYouWantToClaim" -> "lowerAmount", "totalCost" -> "100"), Nil),
      (agentFormProvider, Map("howMuchDoYouWantToClaim" -> "lowerAmount", "totalCost" -> "100"), Nil),
      (individualFormProvider, Map("howMuchDoYouWantToClaim" -> "wrongValue"), invalid),
      (agentFormProvider, Map("howMuchDoYouWantToClaim" -> "wrongValue"), invalid),
      (individualFormProvider, emptyData, individualRequired),
      (agentFormProvider, emptyData, agentRequired),
      (individualFormProvider, lowerAmountMap, amountRequired),
      (individualFormProvider, lowerAmountMap + ("totalCost" -> "1000"), Nil),
      (individualFormProvider, lowerAmountMap + ("totalCost" -> "1"), Nil),
      (individualFormProvider, lowerAmountMap + ("totalCost" -> "0"), lessThanZero),
      (individualFormProvider, lowerAmountMap + ("totalCost" -> "abc"), nonNumeric),
      (individualFormProvider, lowerAmountMap + ("totalCost" -> "123.30"), noDecimals),
      (individualFormProvider, lowerAmountMap + ("totalCost" -> "2000"),overMax)
    )
    // @formatter:on

    "correctly bind a form" in forAll(cases) { case (formProvider, data, expectedErrors) =>
      val form = formProvider.bind(data)
      assert(form.errors.toList === expectedErrors)
    }

    "bind a form if not lowerAmount selected" in {
      val data = Map("howMuchDoYouWantToClaim" -> "fullCost", "totalCost" -> "100000000000000000")
      assert(individualFormProvider.bind(data).errors.toList === Nil)
      assert(agentFormProvider.bind(data).errors.toList === Nil)
    }
  }

}

object ZegvHowMuchDoYouWantToClaimFormProviderSpec {
  val fullAmount             = BigDecimal(1000)
  val emptyData              = Map[String, String]()
  val individualFormProvider = ZegvHowMuchDoYouWantToClaimFormProvider.apply(UserType.Individual, fullAmount)
  val agentFormProvider      = ZegvHowMuchDoYouWantToClaimFormProvider.apply(UserType.Agent, fullAmount)
  val lowerAmountMap         = Map("howMuchDoYouWantToClaim" -> "lowerAmount")
}
