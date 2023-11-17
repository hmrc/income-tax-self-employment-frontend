package base

import controllers.standard.routes
import models.common.{UserType, onwardRoute}
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, redirectLocation, route, running, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

abstract class CYAControllerBaseSpec(controllerName: String) extends ControllerSpec {

  protected val onPageLoadRoute: String
  protected val userAnswers: UserAnswers

  def expectedSummaryList(authUserType: UserType)(implicit messages: Messages): SummaryList

  def expectedView(scenario: TestScenario, summaryList: SummaryList, nextRoute: String)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String

  protected implicit lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoadRoute)

  protected val nextRoute: String = onwardRoute.url

  s"$controllerName" - {
    "loading a page" - {
      "answers for the user exist" - {
        forAll(langUserTypeCases) { (lang, userType) =>
          s"language is $lang and user is an $userType" - {
            "return a 200 OK with answered questions present as rows in view" in new TestScenario(userType, Some(userAnswers)) {
              running(application) {
                val result = languageAwareResult(lang, route(application, getRequest).value)

                status(result) shouldBe OK
                contentAsString(result) mustEqual expectedView(this, expectedSummaryList(userType), nextRoute)
              }
            }
          }
        }
      }
      "no user answers exist" - {
        forAll(userTypeCases) { userType =>
          s"user is an $userType" - {
            "redirect to the journey recovery controller" in new TestScenario(userType, None) {
              running(application) {
                val result = route(application, getRequest).value

                status(result) shouldBe SEE_OTHER
                redirectLocation(result).value shouldBe routes.JourneyRecoveryController.onPageLoad().url
              }
            }
          }
        }
      }
    }
  }

}
