package viewmodels.checkAnswers

import base.SpecBase._
import models.common.UserType
import org.scalatest.wordspec.AnyWordSpecLike
import pages.OneQuestionPage
import play.api.libs.json.Json

class BigDecimalSummarySpec extends AnyWordSpecLike {
  object TestPage extends OneQuestionPage[BigDecimal] {
    override def toString: String = "someBigDecimalPage"
  }

  "row" should {
    UserType.values.foreach { userType =>
      s"return a SummaryListRow for $userType" in {
        val summary = new BigDecimalSummary(TestPage, call)
        val answers = buildUserAnswers(
          Json.obj(TestPage.pageName.value -> "12344321.0")
        )

        val result = summary.row(answers, taxYear, businessId, userType)(messagesStubbed).value
        assert(result.key.content.asHtml.toString() === s"someBigDecimalPage.subHeading.cya.$userType")
        assert(result.value.content.asHtml.toString() === "£12,344,321.00")
      }
    }
  }
}
