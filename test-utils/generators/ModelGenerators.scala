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

import models._
import models.journeys._
import models.journeys.expenses.{AdvertisingOrMarketing, Depreciation, DisallowableProfessionalFees, DisallowableStaffCosts, DisallowableSubcontractorCosts, EntertainmentCosts, FinancialExpenses, OtherExpenses, ProfessionalServiceExpenses, TravelForWork}
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryTravelForWork: Arbitrary[TravelForWork] =
    Arbitrary {
      Gen.oneOf(TravelForWork.values.toSeq)
    }

  implicit lazy val arbitraryProfessionalServiceExpenses: Arbitrary[ProfessionalServiceExpenses] =
    Arbitrary {
      Gen.oneOf(ProfessionalServiceExpenses.values)
    }

  implicit lazy val arbitraryEntertainmentCosts: Arbitrary[EntertainmentCosts] =
    Arbitrary {
      Gen.oneOf(EntertainmentCosts.values.toSeq)
    }

  implicit lazy val arbitraryDisallowableSubcontractorCosts: Arbitrary[DisallowableSubcontractorCosts] =
    Arbitrary {
      Gen.oneOf(DisallowableSubcontractorCosts.values.toSeq)
    }

  implicit lazy val arbitraryDisallowableStaffCosts: Arbitrary[DisallowableStaffCosts] =
    Arbitrary {
      Gen.oneOf(DisallowableStaffCosts.values.toSeq)
    }

  implicit lazy val arbitraryDisallowableProfessionalFees: Arbitrary[DisallowableProfessionalFees] =
    Arbitrary {
      Gen.oneOf(DisallowableProfessionalFees.values.toSeq)
    }

  implicit lazy val arbitraryAdvertisingOrMarketing: Arbitrary[AdvertisingOrMarketing] =
    Arbitrary {
      Gen.oneOf(AdvertisingOrMarketing.values.toSeq)
    }

  implicit lazy val arbitraryDepreciation: Arbitrary[Depreciation] =
    Arbitrary {
      Gen.oneOf(Depreciation.values.toSeq)
    }

  implicit lazy val arbitraryOtherExpenses: Arbitrary[OtherExpenses] =
    Arbitrary {
      Gen.oneOf(OtherExpenses.values.toSeq)
    }

  implicit lazy val arbitraryFinancialExpenses: Arbitrary[FinancialExpenses] =
    Arbitrary {
      Gen.oneOf(FinancialExpenses.values)
    }

  implicit lazy val arbitraryWorkFromHome: Arbitrary[WorkFromHome] =
    Arbitrary {
      Gen.oneOf(WorkFromHome.values.toSeq)
    }

  implicit lazy val arbitraryWorkFromBusinessPremises: Arbitrary[WorkFromBusinessPremises] =
    Arbitrary {
      Gen.oneOf(WorkFromBusinessPremises.values.toSeq)
    }

  implicit lazy val arbitraryTaxiMinicabOrRoadHaulage: Arbitrary[TaxiMinicabOrRoadHaulage] =
    Arbitrary {
      Gen.oneOf(TaxiMinicabOrRoadHaulage.values.toSeq)
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

  implicit lazy val arbitraryCompletedSectionState: Arbitrary[CompletedSectionState] =
    Arbitrary {
      Gen.oneOf(CompletedSectionState.values)
    }

}
