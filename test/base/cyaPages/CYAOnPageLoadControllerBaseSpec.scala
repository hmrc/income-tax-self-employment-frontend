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

package base.cyaPages

import base.SpecBase
import common.TestApp.buildAppFromUserType
import controllers.standard.routes
import models.common.UserType.Individual
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.standard.CheckYourAnswersView

import scala.concurrent.Future

trait CYAOnPageLoadControllerBaseSpec extends CYAControllerBaseSpec {

  val pageHeading: String
  val testDataCases: List[JsObject]

  def onPageLoadCall: (TaxYear, BusinessId) => Call

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList

  def createExpectedView(userType: UserType, summaryList: SummaryList, messages: Messages, application: Application, request: Request[_]): String = {
    val view = application.injector.instanceOf[CheckYourAnswersView]
    view(pageHeading, taxYear, userType, summaryList, onSubmitCall(taxYear, businessId))(request, messages).toString()
  }

  "onPageLoad" - {
    "when answers for the user exist" - {
      userTypes.foreach { userType =>
        s"should return Ok and render correct view for various data when user is an $userType" in {
          testDataCases.foreach { data =>
            val application   = buildAppFromUserType(userType, Some(buildUserAnswers(data)))
            val msg: Messages = SpecBase.messages(application)

            implicit val impMsg: Messages = SpecBase.messages(application)

            val onPageLoadRequest        = FakeRequest(GET, onPageLoadCall(taxYear, businessId).url)
            val summaryList: SummaryList = expectedSummaryList(buildUserAnswers(data), taxYear, businessId, userType)

            val result = route(application, onPageLoadRequest).value
            val expectedResult =
              createExpectedView(userType, summaryList, msg, application, onPageLoadRequest)

            status(result) mustBe OK
            contentAsString(result) mustEqual expectedResult
          }
        }
      }
    }
    "when no user answers exist" - {
      "should redirect to the journey recovery controller" in {
        val application            = buildAppFromUserType(Individual, None)
        val onPageLoadRequest      = FakeRequest(GET, onPageLoadCall(taxYear, businessId).url)
        val result: Future[Result] = route(application, onPageLoadRequest).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
