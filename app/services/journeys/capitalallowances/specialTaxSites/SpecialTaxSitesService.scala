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

import cats.implicits.catsSyntaxOptionId
import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.capitalallowances.specialTaxSites.NewSpecialTaxSite.newSite
import models.journeys.capitalallowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation}
import pages.capitalallowances.specialTaxSites._
import repositories.SessionRepositoryBase

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SpecialTaxSitesService @Inject() (sessionRepository: SessionRepositoryBase)(implicit ec: ExecutionContext) {

  def submitAnswer[A](userAnswers: UserAnswers,
                      answer: A,
                      businessId: BusinessId,
                      index: Int,
                      page: SpecialTaxSitesBasePage[A]): Future[UserAnswers] = {
    val listOfSites: Option[List[NewSpecialTaxSite]] = userAnswers.get(NewSpecialTaxSitesList, Some(businessId))
    val siteOfIndex: Option[NewSpecialTaxSite]       = listOfSites.map(_(index))
    val isFirstPage: Boolean                         = page == ContractForBuildingConstructionPage
    val indexIsValidForNewSite                       = (list: List[NewSpecialTaxSite]) => index == 0 || list.length == index
    val updatedList = (listOfSites, siteOfIndex) match {
      case (None, None) if index == 0 && isFirstPage =>
        updateSiteAndList(newSite(), List(newSite()), page, answer, index) // make a new list with a new empty site and save first page answer
      case (Some(list), None) if indexIsValidForNewSite(list) =>
        updateSiteAndList(newSite(), list, page, answer, index) // making a new site, appended to list
      case (Some(list), Some(site)) => updateSiteAndList(site, list, page, answer, index) // editing existing site in list
      case _                        => ???                                                // error
    }

    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(NewSpecialTaxSitesList, updatedList, Some(businessId)))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers
  }

  private def updateSiteAndList[A](site: NewSpecialTaxSite,
                                   list: List[NewSpecialTaxSite],
                                   page: SpecialTaxSitesBasePage[A],
                                   answer: A,
                                   index: Int): List[NewSpecialTaxSite] = {
    val updatedSite = updateSite(site, page, answer)
    val updatedList = list.updated(index, updatedSite)
    updatedList
  }

  private def updateSite[A](site: NewSpecialTaxSite, page: SpecialTaxSitesBasePage[A], answer: A): NewSpecialTaxSite =
    (page, answer) match {
      case (ContractForBuildingConstructionPage, answer: Boolean)       => site.copy(contractForBuildingConstruction = answer.some)
      case (ContractStartDatePage, answer: LocalDate)                   => site.copy(contractStartDate = answer.some)
      case (ConstructionStartDatePage, answer: LocalDate)               => site.copy(constructionStartDate = answer.some)
      case (QualifyingUseStartDatePage, answer: LocalDate)              => site.copy(qualifyingUseStartDate = answer.some)
      case (SpecialTaxSiteLocationPage, answer: SpecialTaxSiteLocation) => site.copy(specialTaxSiteLocation = answer.some)
      case (NewSiteClaimingAmountPage, answer: BigDecimal)              => site.copy(newSiteClaimingAmount = answer.some)
      case _                                                            => ???
    }
}
