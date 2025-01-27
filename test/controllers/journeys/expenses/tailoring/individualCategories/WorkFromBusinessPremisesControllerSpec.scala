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
import models.common.AccountingType.Accrual
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import models.journeys.expenses.individualCategories.GoodsToSellOrUse.YesDisallowable
import models.journeys.expenses.individualCategories.{GoodsToSellOrUse, RepairsAndMaintenance, WorkFromBusinessPremises}
import models.{CheckMode, NormalMode}
import navigation.{ExpensesTailoringNavigator, FakeExpensesTailoringNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.TradeAccountingType
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.individualCategories._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.journeys.expenses.tailoring.individualCategories.WorkFromBusinessPremisesView

import scala.concurrent.Future

class WorkFromBusinessPremisesControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val workFromBusinessPremisesRoute: String = routes.WorkFromBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode).url

  val formProvider                            = new EnumerableFormProvider()
  val page: WorkFromBusinessPremisesPage.type = WorkFromBusinessPremisesPage

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  override def beforeEach(): Unit = {
    reset(mockService)
    super.beforeEach()
  }

  case class UserScenario(userType: UserType)

  val userScenarios: Seq[UserScenario] = Seq(
    UserScenario(Individual),
    UserScenario(Agent)
  )

  def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      ExpensesCategoriesPage.toString    -> IndividualCategories.toString,
      TradeAccountingType.toString       -> Accrual.toString,
      OfficeSuppliesPage.toString        -> YesDisallowable.toString,
      GoodsToSellOrUsePage.toString      -> GoodsToSellOrUse.YesDisallowable.toString,
      RepairsAndMaintenancePage.toString -> RepairsAndMaintenance.YesDisallowable.toString,
      WorkFromHomePage.toString          -> true
    )
  )

  "WorkFromBusinessPremises Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {

          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(baseAnswers), userScenario.userType).build()
            val form        = formProvider(page, userScenario.userType)

            running(application) {
              val request = FakeRequest(GET, workFromBusinessPremisesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val expectedResult =
                view(form, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers =
              baseAnswers
                .set(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.values.head, Some(businessId))
                .success
                .value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.userType).build()
            val form        = formProvider(page, userScenario.userType)

            running(application) {
              val request = FakeRequest(GET, workFromBusinessPremisesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val expectedResult =
                view(form.fill(WorkFromBusinessPremises.values.head), NormalMode, userScenario.userType, taxYear, businessId)(
                  request,
                  messages(application)).toString

              status(result) mustEqual OK
              contentAsString(result) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, workFromBusinessPremisesRoute)

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
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, workFromBusinessPremisesRoute)
              .withFormUrlEncodedBody(("value", WorkFromBusinessPremises.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when user is an ${userScenario.userType}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType).build()
            val form        = formProvider(page, userScenario.userType)

            running(application) {
              val request =
                FakeRequest(POST, workFromBusinessPremisesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType).build()
            val form        = formProvider(page, userScenario.userType)

            running(application) {
              val request =
                FakeRequest(POST, workFromBusinessPremisesRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[WorkFromBusinessPremisesView]

              val result = route(application, request).value

              val expectedResult =
                view(boundForm, NormalMode, userScenario.userType, taxYear, businessId)(request, messages(application)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual expectedResult
            }
          }

          "must redirect to the next page when valid data is submitted in CheckMode" in {

            val ua = emptyUserAnswersAccrual.set(WorkFromBusinessPremisesPage, WorkFromBusinessPremises.YesDisallowable).success.value
            val application =
              applicationBuilder(userAnswers = Some(ua))
                .overrides(
                  bind[ExpensesTailoringNavigator].toInstance(new FakeExpensesTailoringNavigator(onwardRoute)),
                  bind[SelfEmploymentService].toInstance(mockService)
                )
                .build()

            running(application) {
              when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(emptyUserAnswers)
              when(mockService.clearWorkplaceRunningCostsExpensesData(anyTaxYear, anyBusinessId)(any, HeaderCarrier(any))) thenReturn EitherT.rightT(
                ())

              val premisesRoute: String = routes.WorkFromBusinessPremisesController.onPageLoad(taxYear, businessId, CheckMode).url

              val request =
                FakeRequest(POST, premisesRoute)
                  .withFormUrlEncodedBody(("value", WorkFromBusinessPremises.YesAllowable.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual onwardRoute.url

              verify(mockService, times(1)).clearWorkplaceRunningCostsExpensesData(anyTaxYear, anyBusinessId)(any, HeaderCarrier(any))
              verify(mockService, times(1)).persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)
            }
          }
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, workFromBusinessPremisesRoute)
              .withFormUrlEncodedBody(("value", WorkFromBusinessPremises.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual standard.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
