/*
 * Copyright 2023 HM Revenue & Customs
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

package base

import controllers.standard.routes
import models.common.{UserType, onwardRoute}
import models.database.UserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import scala.concurrent.Future

abstract class CYAControllerBaseSpec(controllerName: String) extends ControllerSpec {

  val onPageLoadRoute: String
  val userAnswers: UserAnswers

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
              val result: Future[Result] = route(application, getRequest).value.map(languageAwareResult(lang, _))

              status(result) shouldBe OK
              contentAsString(result) mustEqual expectedView(this, expectedSummaryList(userType), nextRoute)
            }
          }
        }
      }
      "no user answers exist" - {
        forAll(userTypeCases) { userType =>
          s"user is an $userType" - {
            "redirect to the journey recovery controller" in new TestScenario(userType, None) {
              val result: Future[Result] = route(application, getRequest).value

              status(result) shouldBe SEE_OTHER
              redirectLocation(result).value shouldBe routes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }
      }
    }
  }

}
