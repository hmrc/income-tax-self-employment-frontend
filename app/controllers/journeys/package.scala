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

package controllers

import models.common.BusinessId
import models.database.UserAnswers
import models.requests.DataRequest
import models.{Mode, NormalMode}
import pages.QuestionPage
import play.api.libs.json.Reads
import queries.Gettable
import services.SelfEmploymentService
import services.SelfEmploymentService.clearDataFromUserAnswers

import scala.concurrent.{ExecutionContext, Future}

package object journeys {

  def clearPagesWhenNo(page: QuestionPage[Boolean], currentAnswer: Boolean, request: DataRequest[_], mode: Mode, businessId: BusinessId)(implicit
      ec: ExecutionContext): Future[(UserAnswers, Mode)] = {
    val clearUserAnswerDataIfNeeded = if (currentAnswer) {
      Future(request.userAnswers)
    } else {
      Future.fromTry(SelfEmploymentService.clearDataFromUserAnswers(request.userAnswers, page.dependentPagesWhenNo, Some(businessId)))
    }
    val redirectMode = determineRedirectModeForNo(page, businessId, mode, request, currentAnswer)
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

  def clearPagesWhenYes(page: QuestionPage[Boolean], currentAnswer: Boolean, request: DataRequest[_], mode: Mode, businessId: BusinessId)(implicit
      ec: ExecutionContext): Future[(UserAnswers, Mode)] = {
    val clearUserAnswerDataIfNeeded = if (currentAnswer) {
      Future.fromTry(SelfEmploymentService.clearDataFromUserAnswers(request.userAnswers, page.dependentPagesWhenYes, Some(businessId)))
    } else {
      Future(request.userAnswers)
    }
    val redirectMode = determineRedirectModeForYes(page, businessId, mode, request, currentAnswer)
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

  private def determineRedirectModeForNo(page: Gettable[Boolean],
                                         businessId: BusinessId,
                                         mode: Mode,
                                         request: DataRequest[_],
                                         currentAnswer: Boolean) =
    request.getValue(page, businessId) match {
      case Some(false) if currentAnswer => NormalMode
      case _                            => mode
    }

  private def determineRedirectModeForYes(page: Gettable[Boolean],
                                          businessId: BusinessId,
                                          mode: Mode,
                                          request: DataRequest[_],
                                          currentAnswer: Boolean) =
    request.getValue(page, businessId) match {
      case Some(true) if !currentAnswer => NormalMode
      case _                            => mode
    }

}
