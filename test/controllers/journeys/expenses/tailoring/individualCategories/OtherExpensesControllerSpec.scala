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
import cats.data.EitherT
import controllers.standard
import forms.standard.EnumerableFormProvider
import models.common.Journey.ExpensesOtherExpenses
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import models.journeys.expenses.individualCategories.ProfessionalServiceExpenses.Staff
import models.journeys.expenses.individualCategories._
import models.{CheckMode, NormalMode}
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories._
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.tailoring.individualCategories.OtherExpensesView

import scala.concurrent.Future

class OtherExpensesControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val otherExpensesRoute: String = routes.OtherExpensesController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider = new EnumerableFormProvider()

  case class UserScenario(userType: UserType, form: Form[OtherExpenses])

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  override def beforeEach(): Unit = {
    reset(mockService)
    super.beforeEach()
  }

  val userScenarios: Seq[UserScenario] = Seq(
    UserScenario(userType = Individual, formProvider(OtherExpensesPage, Individual)),
    UserScenario(userType = Agent, formProvider(OtherExpensesPage, Agent))
  )

  def baseAnswers: UserAnswers = buildUserAnswers(
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
      FinancialExpensesPage.toString           -> List(FinancialExpenses.NoFinancialExpenses.toString),
      DepreciationPage.toString                -> true
    )
  )

  "OtherExpenses Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()

            val request = FakeRequest(GET, otherExpensesRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[OtherExpensesView]

            val expectedResult =
              view(userScenario.form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedResult
            application.stop()

          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = baseAnswers.set(OtherExpensesPage, OtherExpenses.values.head, Some(businessId)).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()

            val request = FakeRequest(GET, otherExpensesRoute)

            val view = application.injector.instanceOf[OtherExpensesView]

            val result = route(application, request).value

            val expectedResult =
              view(userScenario.form.fill(OtherExpenses.values.head), NormalMode, userScenario.userType, taxYear, businessId)(
                request,
                messages(application)).toString

            status(result) mustEqual OK
            contentAsString(result) mustEqual expectedResult
            application.stop()
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, otherExpensesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        application.stop()
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

        val request =
          FakeRequest(POST, otherExpensesRoute)
            .withFormUrlEncodedBody(("value", OtherExpenses.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()

            val request =
              FakeRequest(POST, otherExpensesRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = userScenario.form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[OtherExpensesView]

            val result = route(application, request).value

            val expectedResult =
              view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual expectedResult
            application.stop()
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()

            val request =
              FakeRequest(POST, otherExpensesRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

            val view = application.injector.instanceOf[OtherExpensesView]

            val result = route(application, request).value

            val expectedResult =
              view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual expectedResult
            application.stop()
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request =
          FakeRequest(POST, otherExpensesRoute)
            .withFormUrlEncodedBody(("value", OtherExpenses.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        application.stop()
      }

      "must redirect to the next page when valid data is submitted in CheckMode" in {

        val ua = emptyUserAnswersAccrual.set(OtherExpensesPage, OtherExpenses.YesAllowable).success.value

        val application =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService)
            )
            .build()

        lazy val professionalServiceExpensesRoute: String =
          controllers.journeys.expenses.tailoring.individualCategories.routes.OtherExpensesController
            .onPageLoad(taxYear, businessId, CheckMode)
            .url

        when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(emptyUserAnswers)
        when(mockService.clearExpensesData(anyTaxYear, anyBusinessId, meq(ExpensesOtherExpenses))(any, any)) thenReturn EitherT.rightT(())

        val request =
          FakeRequest(POST, professionalServiceExpensesRoute)
            .withFormUrlEncodedBody(("value", OtherExpenses.No.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockService, times(1)).persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)
        verify(mockService, times(1)).clearExpensesData(anyTaxYear, anyBusinessId, meq(ExpensesOtherExpenses))(any, any)
        application.stop()
      }

    }
  }

}
