package viewmodels.checkAnswers

import base.SpecBase._
import models.common.UserType
import org.scalatest.wordspec.AnyWordSpecLike
import pages.OneQuestionPage
import play.api.libs.json.Json

class BooleanSummarySpec extends AnyWordSpecLike {
  object TestPage extends OneQuestionPage[Boolean] {
    override def toString: String = "someBooleanPage"
  }

  "row" should {
    UserType.values.foreach { userType =>
      List(true, false).foreach { answer =>
        s"return a SummaryListRow for $userType and answer=$answer" in {
          val summary = new BooleanSummary(TestPage, call)
          val answers = buildUserAnswers(
            Json.obj(TestPage.pageName.value -> answer)
          )

          val result = summary.row(answers, taxYear, businessId, userType)(messagesStubbed).value
          assert(result.key.content.asHtml.toString() === s"someBooleanPage.subHeading.cya.$userType")
          val expectedAnswer = if (answer) "site.yes" else "site.no"
          assert(result.value.content.asHtml.toString() === expectedAnswer)
        }
      }
    }
  }
}
