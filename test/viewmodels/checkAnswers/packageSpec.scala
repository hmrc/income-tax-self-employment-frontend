package viewmodels.checkAnswers

import base.SpecBase._
import models.common.UserType.Individual
import org.scalatest.wordspec.AnyWordSpecLike
import pages.Page

class packageSpec extends AnyWordSpecLike {
  object StubPage extends Page {
    override def toString: String = "stubPage"
  }

  "mkBooleanSummary" should {
    List(true, false).foreach { answer =>
      s"build boolean summary row for answer=$answer" in {
        val result = mkBooleanSummary(answer = answer, call, StubPage, Individual)(messagesStubbed)
        assert(result.key.content.asHtml.toString() === "stubPage.subHeading.cya.individual")
        assert(result.value.content.asHtml.toString() === s"site.${if (answer) "yes" else "no"}")
      }
    }
  }

  "mkBigDecimalSummary" should {
    "build big decimal summary row" in {
      val result = mkBigDecimalSummary(answer = BigDecimal(10.0), call, StubPage, Individual)(messagesStubbed)
      assert(result.key.content.asHtml.toString() === "stubPage.subHeading.cya.individual")
      assert(result.value.content.asHtml.toString() === "£10.00")
    }
  }
}
