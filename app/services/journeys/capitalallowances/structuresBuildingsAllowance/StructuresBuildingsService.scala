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

package services.journeys.capitalallowances.structuresBuildingsAllowance

import cats.implicits.catsSyntaxOptionId
import controllers.journeys.clearDependentPages
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding.newStructure
import models.journeys.capitalallowances.structuresBuildingsAllowance.{NewStructureBuilding, StructuresBuildingsLocation}
import models.requests.DataRequest
import pages.capitalallowances.structuresBuildingsAllowance._
import play.api.mvc.Result
import repositories.SessionRepositoryBase
import services.SelfEmploymentService

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StructuresBuildingsService @Inject() (sessionRepository: SessionRepositoryBase, service: SelfEmploymentService)(implicit ec: ExecutionContext) {

  private[structuresBuildingsAllowance] def submitAnswerAndClearDependentAnswers(pageUpdated: StructuresBuildingsBasePage[Boolean],
                                                                                 businessId: BusinessId,
                                                                                 request: DataRequest[_],
                                                                                 newAnswer: Boolean): Future[UserAnswers] =
    for {
      editedUserAnswers  <- clearDependentPages(pageUpdated, newAnswer, request, businessId)
      updatedUserAnswers <- service.persistAnswer(businessId, editedUserAnswers, newAnswer, pageUpdated)
    } yield updatedUserAnswers

  def submitAnswerAndRedirect(pageUpdated: StructuresBuildingsBasePage[Boolean],
                              businessId: BusinessId,
                              request: DataRequest[_],
                              newAnswer: Boolean,
                              taxYear: TaxYear,
                              mode: Mode): Future[Result] =
    submitAnswerAndClearDependentAnswers(pageUpdated, businessId, request, newAnswer)
      .map { updatedAnswers =>
        pageUpdated.redirectNext(mode, updatedAnswers, businessId, taxYear)
      }

  def updateStructureAnswerWithIndex[A](userAnswers: UserAnswers,
                                        answer: A,
                                        businessId: BusinessId,
                                        index: Int,
                                        page: StructuresBuildingsBasePage[A]): Future[UserAnswers] = {
    val listOfStructures: Option[List[NewStructureBuilding]] = userAnswers.get(NewStructuresBuildingsList, Some(businessId))
    val siteOfIndex: Option[NewStructureBuilding]            = if (listOfStructures.exists(_.length > index)) listOfStructures.map(_(index)) else None
    val isFirstPageOfLoop: Boolean                           = page == StructuresBuildingsQualifyingUseDatePage
    val isValidIndexForNewStructure                          = (list: List[NewStructureBuilding]) => index == 0 || list.length == index
    val updatedList = (listOfStructures, siteOfIndex) match {
      case (None, None) if index == 0 && isFirstPageOfLoop =>
        updateStructureAndList(
          newStructure,
          List(newStructure),
          page,
          answer,
          index
        ) // make a new list with a new empty site and save first page answer
      case (Some(list), None) if isValidIndexForNewStructure(list) =>
        updateStructureAndList(newStructure, list.appended(newStructure), page, answer, index) // making a new site, appended to list
      case (Some(list), Some(structure)) =>
        updateStructureAndList(structure, list, page, answer, index) // editing existing site in list
      case _ => listOfStructures.getOrElse(List.empty) // error
    }

    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(NewStructuresBuildingsList, updatedList, Some(businessId)))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers
  }

  private def updateStructureAndList[A](structure: NewStructureBuilding,
                                        list: List[NewStructureBuilding],
                                        page: StructuresBuildingsBasePage[A],
                                        answer: A,
                                        index: Int): List[NewStructureBuilding] = {
    val updatedStructure: NewStructureBuilding =
      (page, answer) match {
        case (StructuresBuildingsQualifyingUseDatePage, answer: LocalDate)          => structure.copy(qualifyingUse = answer.some)
        case (StructuresBuildingsLocationPage, answer: StructuresBuildingsLocation) => structure.copy(newStructureBuildingLocation = answer.some)
        case (StructuresBuildingsNewClaimAmountPage, answer: BigDecimal) => structure.copy(newStructureBuildingClaimingAmount = answer.some)
        case _                                                           => newStructure
      }
    list.updated(index, updatedStructure)
  }

}
