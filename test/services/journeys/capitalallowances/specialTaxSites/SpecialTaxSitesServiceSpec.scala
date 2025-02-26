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

package services.journeys.capitalallowances.specialTaxSites

import base.SpecBase
import cats.implicits.catsSyntaxOptionId
import data.TimeData
import models.database.UserAnswers
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite.newSite
import models.journeys.capitalallowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.capitalallowances.specialTaxSites.{
  ContractForBuildingConstructionPage,
  NewSiteClaimingAmountPage,
  NewSpecialTaxSitesList,
  SpecialTaxSitesPage
}
import repositories.SessionRepository
import utils.TimeMachine

import scala.concurrent.Future

class SpecialTaxSitesServiceSpec extends SpecBase {

  val mockRepository: SessionRepository = mock[SessionRepository]
  val underTest                         = new SpecialTaxSitesService(mockRepository)
  val mockTimeMachine: TimeMachine      = mock[TimeMachine]

  when(mockTimeMachine.now).thenReturn(TimeData.testDate)

  val existingSitesList: List[NewSpecialTaxSite] = List(newSite)
  val answersWithEmptySite: UserAnswers          = buildUserAnswers(NewSpecialTaxSitesList, existingSitesList)
  val completedSite: NewSpecialTaxSite = NewSpecialTaxSite(
    true.some,
    mockTimeMachine.now.some,
    mockTimeMachine.now.some,
    mockTimeMachine.now.some,
    BigDecimal(20000).some,
    SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA").some,
    BigDecimal(10000).some
  )
  val validIndex         = 0
  val invalidIndex       = 3
  val amount: BigDecimal = 9999.99
  val booleanAnswer      = false

  when(mockRepository.set(any())) thenReturn Future.successful(true)

  "updateSiteAnswerWithIndex" - {
    "should return an updated UserAnswers" - {
      "when there is no existing list and index is valid (0), when answering the first question" in {

        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            emptyUserAnswersAccrual,
            booleanAnswer,
            businessId,
            validIndex,
            ContractForBuildingConstructionPage
          )
          .futureValue
          .data
        val expectedAnswer = buildUserAnswers(NewSpecialTaxSitesList, List(NewSpecialTaxSite(Some(booleanAnswer)))).data

        assert(updatedAnswers == expectedAnswer)
      }
      "when creating a new site to an existing list" in {

        val existingSitesList = List(completedSite)
        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            buildUserAnswers(NewSpecialTaxSitesList, existingSitesList),
            booleanAnswer,
            businessId,
            existingSitesList.length,
            ContractForBuildingConstructionPage
          )
          .futureValue
          .data
        val expectedAnswer = buildUserAnswers(NewSpecialTaxSitesList, List(completedSite, NewSpecialTaxSite(Some(booleanAnswer)))).data

        assert(updatedAnswers == expectedAnswer)
      }
      "when updating an existing Site with a valid index, when adding a new answer" in {

        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            buildUserAnswers(NewSpecialTaxSitesList, existingSitesList),
            booleanAnswer,
            businessId,
            validIndex,
            ContractForBuildingConstructionPage
          )
          .futureValue
          .data
        val expectedAnswer = buildUserAnswers(NewSpecialTaxSitesList, List(NewSpecialTaxSite(Some(booleanAnswer)))).data

        assert(updatedAnswers == expectedAnswer)
      }
      "when updating an existing Site with a valid index, when updating an existing answer" in {

        val existingSitesList = List(completedSite)
        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            buildUserAnswers(NewSpecialTaxSitesList, existingSitesList),
            amount,
            businessId,
            validIndex,
            NewSiteClaimingAmountPage
          )
          .futureValue
          .data
        val expectedAnswer = buildUserAnswers(NewSpecialTaxSitesList, List(completedSite.copy(newSiteClaimingAmount = Some(amount)))).data

        assert(updatedAnswers == expectedAnswer)
      }
    }

    "should return the UserAnswers with an empty site" - {
      "when the page is invalid" in {

        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            buildUserAnswers(NewSpecialTaxSitesList, existingSitesList),
            booleanAnswer,
            businessId,
            validIndex,
            SpecialTaxSitesPage
          )
          .futureValue
          .data
        val expectedAnswer = answersWithEmptySite.data

        assert(updatedAnswers == expectedAnswer)
      }
      "when the index is invalid" in {

        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            buildUserAnswers(NewSpecialTaxSitesList, existingSitesList),
            booleanAnswer,
            businessId,
            invalidIndex,
            ContractForBuildingConstructionPage
          )
          .futureValue
          .data
        val expectedAnswer = answersWithEmptySite.data

        assert(updatedAnswers == expectedAnswer)
      }
      "when creating a new site but not submitting the first question" in {

        val updatedAnswers = underTest
          .updateSiteAnswerWithIndex(
            buildUserAnswers(NewSpecialTaxSitesList, existingSitesList),
            amount,
            businessId,
            invalidIndex,
            NewSiteClaimingAmountPage
          )
          .futureValue
          .data
        val expectedAnswer = answersWithEmptySite.data

        assert(updatedAnswers == expectedAnswer)
      }
    }
  }
}
