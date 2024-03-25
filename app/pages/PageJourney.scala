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

package pages

import models.common.BusinessId
import models.database.UserAnswers
import pages.PageJourney.{PageWithQuestion, TerminalPage}

sealed trait PageJourney {
  def page: Page

  def maybeNextPage(answers: UserAnswers, businessId: BusinessId): Option[PageJourney] = {
    val maybeNext = this match {
      case x: PageWithQuestion => Some(x)
      case _: TerminalPage     => None
    }

    maybeNext.flatMap(_.page.next(answers, businessId))
  }
}

object PageJourney {
  final case class PageWithQuestion(page: QuestionPage[_]) extends PageJourney
  final case class TerminalPage(page: Page)                extends PageJourney

  def mkQuestion(page: Page): PageJourney =
    page match {
      case p: OneQuestionPage[_] => PageWithQuestion(p)
      case p: Page               => TerminalPage(p)
    }
}
