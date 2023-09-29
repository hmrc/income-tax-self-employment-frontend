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
import connectors.SelfEmploymentConnector
import controllers.journeys.routes
import models.errors.{HttpError, HttpErrorBody}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.journeys.TaskListView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase with MockitoSugar {

  "Check Your Answers Controller" - {

    val mockConnector = mock[SelfEmploymentConnector]

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
        .build()

      running(application) {
        when(mockConnector.getJourneyState(any, any, any, any)(any, any)) thenReturn Future(Right(None))

        val taxYear = LocalDate.now().getYear
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxYear, "status.checkOurRecords")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when an error response is returned from the service" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector))
          .build()

      running(application) {
        when(mockConnector.getJourneyState(any, any, any, any)(any, any)) thenReturn
          Future(Left(HttpError(BAD_REQUEST, HttpErrorBody.SingleErrorBody("500", "Server Error"))))

        val taxYear = LocalDate.now().getYear
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad(taxYear).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
