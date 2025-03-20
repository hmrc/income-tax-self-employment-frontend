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

package generators

import models.VehicleType
import models.journeys.expenses.individualCategories._
import models.journeys.income.{HowMuchTradingAllowance, TradingAllowance}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryVehicleType: Arbitrary[VehicleType] =
    Arbitrary {
      Gen.oneOf(VehicleType.values)
    }

  implicit lazy val arbitraryTravelForWork: Arbitrary[TravelForWork] =
    Arbitrary {
      Gen.oneOf(TravelForWork.values)
    }

  implicit lazy val arbitraryProfessionalServiceExpenses: Arbitrary[ProfessionalServiceExpenses] =
    Arbitrary {
      Gen.oneOf(ProfessionalServiceExpenses.values)
    }

  implicit lazy val arbitraryAdvertisingOrMarketing: Arbitrary[AdvertisingOrMarketing] =
    Arbitrary {
      Gen.oneOf(AdvertisingOrMarketing.values.toSeq)
    }

  implicit lazy val arbitraryOtherExpenses: Arbitrary[OtherExpenses] =
    Arbitrary {
      Gen.oneOf(OtherExpenses.values.toSeq)
    }

  implicit lazy val arbitraryFinancialExpenses: Arbitrary[FinancialExpenses] =
    Arbitrary {
      Gen.oneOf(FinancialExpenses.values)
    }

  implicit lazy val arbitraryWorkFromBusinessPremises: Arbitrary[WorkFromBusinessPremises] =
    Arbitrary {
      Gen.oneOf(WorkFromBusinessPremises.values.toSeq)
    }

  implicit lazy val arbitraryRepairsAndMaintenance: Arbitrary[RepairsAndMaintenance] =
    Arbitrary {
      Gen.oneOf(RepairsAndMaintenance.values.toSeq)
    }

  implicit lazy val arbitraryOfficeSupplies: Arbitrary[OfficeSupplies] =
    Arbitrary {
      Gen.oneOf(OfficeSupplies.values.toSeq)
    }

  implicit lazy val arbitraryGoodsToSellOrUse: Arbitrary[GoodsToSellOrUse] =
    Arbitrary {
      Gen.oneOf(GoodsToSellOrUse.values.toSeq)
    }

  implicit lazy val arbitraryHowMuchTradingAllowance: Arbitrary[HowMuchTradingAllowance] =
    Arbitrary {
      Gen.oneOf(HowMuchTradingAllowance.values)
    }

  implicit lazy val arbitraryTradingAllowance: Arbitrary[TradingAllowance] =
    Arbitrary {
      Gen.oneOf(TradingAllowance.values)
    }

}
