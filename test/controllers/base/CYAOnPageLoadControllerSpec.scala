package controllers.base

import base.SpecBase
import base.SpecBase.{taxYear, _}
import common.TestApp.buildAppFromUserAnswers
import models.common.{BusinessId, Language, TaxYear}
import models.database.UserAnswers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

trait CYAOnPageLoadControllerSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {
  type OnPageLoadView = (Messages, Application, Request[_]) => String

  def onPageLoad: (TaxYear, BusinessId) => Call
  def onPageLoadCases: TableFor2[JsObject, OnPageLoadView]

  "onPageLoad" should {
    "return Ok and render correct view for various data" in {
      forAll(onPageLoadCases) { case (userAnswersData, expectedView) =>
        val userAnswers          = UserAnswers(userAnswersId, userAnswersData)
        val application          = buildAppFromUserAnswers(userAnswers)
        val msg: Messages        = SpecBase.messages(application, Language.English)
        val getOnPageLoadRequest = FakeRequest(GET, onPageLoad(taxYear, stubBusinessId).url)

        val result = route(application, getOnPageLoadRequest).value

        status(result) mustBe OK
        contentAsString(result) mustEqual expectedView(msg, application, getOnPageLoadRequest)
      }
    }
  }

}
