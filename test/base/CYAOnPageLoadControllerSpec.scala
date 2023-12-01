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

import base.SpecBase._
import common.TestApp.buildAppFromUserType
import models.common.UserType.{Agent, Individual}
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import pages.Page
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.standard.CheckYourAnswersView

trait CYAOnPageLoadControllerSpec extends AnyWordSpecLike {

  val userTypes: List[UserType] = List(Individual, Agent)

  val page: Page
  def onPageLoadCall: (TaxYear, BusinessId) => Call
  def onSubmitCall: (TaxYear, BusinessId) => Call

  def getSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): SummaryList

  val testDataCases: List[JsObject]

  def userAnswers(data: JsObject): UserAnswers = UserAnswers(userAnswersId, Json.obj(businessId.value -> data))

  def createExpectedView(userType: UserType, summaryList: SummaryList, msg: Messages, application: Application, request: Request[_]): String = {
    val view = application.injector.instanceOf[CheckYourAnswersView]
    view(page.pageName.value, taxYear, userType, summaryList, onSubmitCall(taxYear, businessId))(request, msg).toString()
  }

  "onPageLoad" should {
    userTypes.foreach { userType =>
      s"return Ok and render correct view for various data when user is an $userType" in {
        testDataCases.foreach { data =>
          val application   = buildAppFromUserType(userType, Some(userAnswers(data)))
          val msg: Messages = SpecBase.messages(application)

          implicit val impMsg: Messages = SpecBase.messages(application)

          val onPageLoadRequest        = FakeRequest(GET, onPageLoadCall(taxYear, businessId).url)
          val summaryList: SummaryList = getSummaryList(userAnswers(data), taxYear, businessId, userType)

          val result = route(application, onPageLoadRequest).value
          val expectedResult =
            createExpectedView(userType, summaryList, msg, application, onPageLoadRequest)

          status(result) mustBe OK
          contentAsString(result) mustEqual expectedResult
        }
      }
    }
  }

}
