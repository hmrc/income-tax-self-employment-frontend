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
import services.SelfEmploymentService.clearDataFromUserAnswers

import scala.concurrent.{ExecutionContext, Future}

package object journeys {
  def clearPagesWhenNo[A: Reads](page: QuestionPage[A], currentAnswer: Boolean, request: DataRequest[_], mode: Mode, businessId: BusinessId)(implicit
      ec: ExecutionContext): Future[(UserAnswers, Mode)] = {
    val clearUserAnswerDataIfNeeded = if (currentAnswer) {
      Future(request.userAnswers)
    } else {
      Future.fromTry(clearDataFromUserAnswers(request.userAnswers, page.pagesToBeCleared, Some(businessId)))
    }
    val redirectMode = determineRedirectMode(page, businessId, mode, request, currentAnswer)
    clearUserAnswerDataIfNeeded.map(editedUserAnswers => (editedUserAnswers, redirectMode))
  }

  private def determineRedirectMode[A: Reads](page: Gettable[A],
                                              businessId: BusinessId,
                                              mode: Mode,
                                              request: DataRequest[_],
                                              currentAnswer: Boolean) =
    request.getValue(page, businessId) match {
      case Some(false) if currentAnswer => NormalMode
      case Some(true) | None            => mode
    }

}
