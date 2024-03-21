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
}
