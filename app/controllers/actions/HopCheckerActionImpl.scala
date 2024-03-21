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

package controllers.actions

import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.Journey
import models.requests.DataRequest
import pages.PageJourney.PageWithQuestion
import pages.{PageJourney, QuestionPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

final case class HopCheckerActionImpl(journey: Journey, targetPage: QuestionPage[_], taxYear: TaxYear, businessId: BusinessId, mode: Mode)(implicit
    ec: ExecutionContext)
    extends ActionFilter[DataRequest] {
  private val start: QuestionPage[_] = journey.startPage

  def executionContext: ExecutionContext = ec

  def filter[A](request: DataRequest[A]): Future[Option[Result]] = {
    val answers = request.userAnswers

    @tailrec
    def isReachableFrom(current: PageJourney): Boolean =
      if (current.page == targetPage) true
      else {
        val nextPage = current.maybeNextPage(answers, businessId)
        nextPage match {
          case None    => false
          case Some(p) => isReachableFrom(p)
        }
      }

    Future.successful {
      if (isReachableFrom(PageWithQuestion(start))) None
      else Some(Redirect(journey.startUrl(taxYear, businessId, mode)))
    }
  }

}
