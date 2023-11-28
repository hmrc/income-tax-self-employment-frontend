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

import SpecBase._
import common.TestApp.buildAppFromUserAnswers
import models.common.{BusinessId, Language, TaxYear}
import models.database.UserAnswers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._

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
        val getOnPageLoadRequest = FakeRequest(GET, onPageLoad(taxYear, businessId).url)

        val result = route(application, getOnPageLoadRequest).value

        status(result) mustBe OK
        contentAsString(result) mustEqual expectedView(msg, application, getOnPageLoadRequest)
      }
    }
  }

}
