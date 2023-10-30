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

package controllers.journeys.expenses

import base.SpecBase
import controllers.journeys.expenses.routes.RepairsAndMaintenanceController
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.RepairsAndMaintenanceFormProvider
import models.journeys.RepairsAndMaintenance
import models.{NormalMode, UserAnswers}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.RepairsAndMaintenancePage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.RepairsAndMaintenanceView

import scala.concurrent.Future

class RepairsAndMaintenanceControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val repairsAndMaintenanceRoute = RepairsAndMaintenanceController.onPageLoad(NormalMode).url

  val formProvider = new RepairsAndMaintenanceFormProvider()
  val businessId   = "SJPR05893938418"

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[RepairsAndMaintenance], accountingType: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formProvider("individual"), "ACCRUAL"),
    UserScenario(isWelsh = false, isAgent = true, formProvider("agent"), "CASH")
  )

  "RepairsAndMaintenance Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, an ${authUserType(userScenario.isAgent)} and using ${userScenario.accountingType} accounting type" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, repairsAndMaintenanceRoute)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[RepairsAndMaintenanceView]

              val expectedResult =
                view(userScenario.form, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              UserAnswers(userAnswersId).set(RepairsAndMaintenancePage, RepairsAndMaintenance.values.head, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request = FakeRequest(GET, repairsAndMaintenanceRoute)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[RepairsAndMaintenanceView]

              val expectedResult =
                view(
                  userScenario.form.fill(RepairsAndMaintenance.values.head),
                  NormalMode,
                  authUserType(userScenario.isAgent),
                  taxYear,
                  businessId,
                  userScenario.accountingType
                )(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, repairsAndMaintenanceRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))

          val request =
            FakeRequest(POST, repairsAndMaintenanceRoute)
              .withFormUrlEncodedBody(("value", RepairsAndMaintenance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${getLanguage(userScenario.isWelsh)}, an ${authUserType(userScenario.isAgent)} and using ${userScenario.accountingType} accounting type" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, repairsAndMaintenanceRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[RepairsAndMaintenanceView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                request,
                messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accountingType))

              val request =
                FakeRequest(POST, repairsAndMaintenanceRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[RepairsAndMaintenanceView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, authUserType(userScenario.isAgent), taxYear, businessId, userScenario.accountingType)(
                request,
                messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" ignore {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, repairsAndMaintenanceRoute)
              .withFormUrlEncodedBody(("value", RepairsAndMaintenance.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

  }

}
