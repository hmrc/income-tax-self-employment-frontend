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

import cats.implicits.catsSyntaxOptionId
import models.common.BusinessId
import models.database.UserAnswers
import models.requests.DataRequest
import pages.{OneQuestionPage, QuestionPage}
import play.api.data.Form
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import services.SelfEmploymentService

import scala.concurrent.{ExecutionContext, Future}

package object journeys {

  def clearDependentPages[A](page: QuestionPage[A], newAnswer: A, userAnswers: UserAnswers, businessId: BusinessId)(implicit
      ec: ExecutionContext,
      reads: Reads[A]): Future[UserAnswers] = {

    val previousAnswer = userAnswers.get(page, Some(businessId))
    val pagesToClear   = (page.dependentPagesWhenYes ++ page.dependentPagesWhenNo ++ page.dependentPagesWhenAnswerChanges).distinct
    if (previousAnswer == Option(newAnswer)) {
      Future.successful(userAnswers)
    } else {
      if (pagesToClear.isEmpty)
        Future(userAnswers)
      else
        Future.fromTry(SelfEmploymentService.clearDataFromUserAnswers(userAnswers, pagesToClear, Some(businessId)))
    }
  }

  def fillForm[A: Reads](page: OneQuestionPage[A], businessId: BusinessId, form: Form[A])(implicit request: DataRequest[AnyContent]): Form[A] =
    request.userAnswers
      .get(page, businessId.some)
      .fold(form)(form.fill)
}
