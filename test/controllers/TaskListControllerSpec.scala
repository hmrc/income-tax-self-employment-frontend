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

package controllers

import base.SpecBase
import builders.UserBuilder.aNoddyUser
import controllers.actions.AuthenticatedIdentifierAction.User
import models.requests.TaggedTradeDetails
import models.viewModels.TaggedTradeDetailsViewModel
import models.viewModels.TaggedTradeDetailsViewModel.buildSummaryList
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TaskListView

import java.time.LocalDate

class TaskListControllerSpec extends SpecBase {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val taxYear = LocalDate.now().getYear
        val selfEmploymentList = Seq(
          TaggedTradeDetails("BusinessId1", Some("TradingName1"), Some(true), Some(false), None, None),
          TaggedTradeDetails("BusinessId2", None, None, None, None, None)
        ).map(x => TaggedTradeDetailsViewModel(x.tradingName.getOrElse(""), x.businessId, buildSummaryList(x, taxYear)(messages(application))))
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, aNoddyUser, selfEmploymentList)(request, messages(application)).toString
      }
    }
  }
}
