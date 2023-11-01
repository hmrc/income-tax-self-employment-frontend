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
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryProfessionalServicesExpenses: Arbitrary[ProfessionalServicesExpenses] =
    Arbitrary {
      Gen.oneOf(ProfessionalServicesExpenses.values)
    }

  implicit lazy val arbitrarydisallowableFees: Arbitrary[DisallowableFees] =
    Arbitrary {
      Gen.oneOf(DisallowableFees.values.toSeq)
    }

  implicit lazy val arbitrarydisallowableIndustryCosts: Arbitrary[DisallowableIndustryCosts] =
    Arbitrary {
      Gen.oneOf(DisallowableIndustryCosts.values.toSeq)
    }

  implicit lazy val arbitrarystaffCosts: Arbitrary[StaffCosts] =
    Arbitrary {
      Gen.oneOf(StaffCosts.values.toSeq)
    }

  implicit lazy val arbitraryEntertainmentCosts: Arbitrary[EntertainmentCosts] =
    Arbitrary {
      Gen.oneOf(EntertainmentCosts.values.toSeq)
    }

  implicit lazy val arbitraryAdvertisingOrMarketing: Arbitrary[AdvertisingOrMarketing] =
    Arbitrary {
      Gen.oneOf(AdvertisingOrMarketing.values.toSeq)
    }

  implicit lazy val arbitraryTravelForWork: Arbitrary[TravelForWork] =
    Arbitrary {
      Gen.oneOf(TravelForWork.values.toSeq)
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
