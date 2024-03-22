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
import pages.{Page, PageJourney, QuestionPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import utils.Logging

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

final case class HopCheckerActionImpl(startPage: QuestionPage[_],
                                      startUrl: String,
                                      targetPage: Page,
                                      taxYear: TaxYear,
                                      businessId: BusinessId,
                                      mode: Mode)(implicit ec: ExecutionContext)
    extends ActionFilter[DataRequest]
    with Logging {

  def executionContext: ExecutionContext = ec

  def filter[A](request: DataRequest[A]): Future[Option[Result]] = {
    val answers = request.userAnswers

    @tailrec
    def findPageWithMissingAnswers(current: PageJourney): Option[PageJourney] =
      if (current.page == targetPage) None
      else {
        val nextPage = current.maybeNextPage(answers, businessId)
        nextPage match {
          case None    => Some(current)
          case Some(p) => findPageWithMissingAnswers(p)
        }
      }

    Future.successful {
      val missingAnswersPage = findPageWithMissingAnswers(PageWithQuestion(startPage))
      missingAnswersPage.fold[Option[Result]](None) { missing =>
        logger.warn(s"Page hopping detected. target: $targetPage, missing answers in a page or reached a terminal page: $missing")
        Some(Redirect(startUrl))
      }
    }
  }

}
