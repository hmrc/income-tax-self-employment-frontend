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

package models.journeys.expenses

import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.OptionValues
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class TravelAndAccommodationExpenseTypeSpec extends AnyFreeSpec with ScalaCheckPropertyChecks with Matchers with OptionValues {

  implicit val arbitraryTravelAndAccommodationExpenseType: Arbitrary[TravelAndAccommodationExpenseType] =
    Arbitrary(Gen.oneOf(TravelAndAccommodationExpenseType.values))

  "TravelAndAccommodationExpenseType" - {

    "must read valid values" in {

      val gen = arbitrary[TravelAndAccommodationExpenseType]

      forAll(gen) { ukAndForeignPropertyRentalTypeUk =>
        JsString(ukAndForeignPropertyRentalTypeUk.toString)
          .validate[TravelAndAccommodationExpenseType]
          .asOpt
          .value mustEqual ukAndForeignPropertyRentalTypeUk
      }
    }

    "must fail to read invalid values" in {

      val gen = arbitrary[String] suchThat (!TravelAndAccommodationExpenseType.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[TravelAndAccommodationExpenseType] mustEqual JsError("error.invalid")
      }
    }

    "must write" in {

      val gen = arbitrary[TravelAndAccommodationExpenseType]

      forAll(gen) { travelAndAccommodationExpenseType =>
        Json.toJson(travelAndAccommodationExpenseType) mustEqual JsString(travelAndAccommodationExpenseType.toString)
      }
    }
  }
}
