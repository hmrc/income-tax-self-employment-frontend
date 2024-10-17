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

package controllers.journeys.expenses.tailoring.individualCategories

import base.SpecBase
import controllers.standard
import forms.standard.BooleanFormProvider
import models.NormalMode
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses.Staff
import models.journeys.expenses.individualCategories._
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories._
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.journeys.expenses.tailoring.individualCategories.DisallowableOtherFinancialChargesView

import scala.concurrent.Future

class DisallowableOtherFinancialChargesControllerSpec extends SpecBase with MockitoSugar {

  implicit val messages: Messages = messagesStubbed

  def onwardRoute = Call("GET", "/foo")

  lazy val disallowableOtherFinancialChargesRoute = routes.DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider = new BooleanFormProvider()

  case class UserScenario(userType: UserType, form: Form[Boolean])

  val userScenarios = Seq(
    UserScenario(userType = Individual, formProvider(DisallowableOtherFinancialChargesPage, Individual)),
    UserScenario(userType = Agent, formProvider(DisallowableOtherFinancialChargesPage, Agent))
  )

  def baseAnswers = buildUserAnswers(
    Json.obj(
      ExpensesCategoriesPage.toString          -> IndividualCategories.toString,
      OfficeSuppliesPage.toString              -> YesDisallowable.toString,
      GoodsToSellOrUsePage.toString            -> GoodsToSellOrUse.YesDisallowable.toString,
      RepairsAndMaintenancePage.toString       -> RepairsAndMaintenance.YesDisallowable.toString,
      WorkFromHomePage.toString                -> true,
      WorkFromBusinessPremisesPage.toString    -> WorkFromBusinessPremises.YesDisallowable.toString,
      TravelForWorkPage.toString               -> TravelForWork.YesDisallowable.toString,
      AdvertisingOrMarketingPage.toString      -> AdvertisingOrMarketing.YesDisallowable.toString,
      EntertainmentCostsPage.toString          -> true,
      ProfessionalServiceExpensesPage.toString -> List(Staff.toString),
      FinancialExpensesPage.toString           -> List(FinancialExpenses.OtherFinancialCharges.toString)
    )
  )

  "DisallowableOtherFinancialCharges Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, disallowableOtherFinancialChargesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[DisallowableOtherFinancialChargesView]

              val expectedResult =
                view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              baseAnswers
                .set(DisallowableOtherFinancialChargesPage, true, Some(businessId))
                .success
                .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            running(application) {
              val request = FakeRequest(GET, disallowableOtherFinancialChargesRoute)

              val view = application.injector.instanceOf[DisallowableOtherFinancialChargesView]

              val result = route(application, request).value

              val expectedResult =
                view(userScenario.form.fill(true), NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, disallowableOtherFinancialChargesRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, disallowableOtherFinancialChargesRoute)
              .withFormUrlEncodedBody(("value", true.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, disallowableOtherFinancialChargesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[DisallowableOtherFinancialChargesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()

            running(application) {
              val request =
                FakeRequest(POST, disallowableOtherFinancialChargesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[DisallowableOtherFinancialChargesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, disallowableOtherFinancialChargesRoute)
              .withFormUrlEncodedBody(("value", true.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

  }

}
