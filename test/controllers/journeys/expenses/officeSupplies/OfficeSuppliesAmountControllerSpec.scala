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

package controllers.journeys.expenses.officeSupplies

import base.SpecBase
import controllers.journeys.expenses.officeSupplies.routes.OfficeSuppliesAmountController
import forms.expenses.officeSupplies.OfficeSuppliesAmountFormProvider
import models.NormalMode
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.officeSupplies.OfficeSuppliesAmountPage
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesAmountView

import scala.concurrent.Future

class OfficeSuppliesAmountControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new OfficeSuppliesAmountFormProvider()
  private val validAnswer  = BigDecimal(100.00)

  private val onwardRoute                            = Call("GET", "/foo")
  private lazy val officeSuppliesAmountPageLoadRoute = OfficeSuppliesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  private lazy val officeSuppliesAmountOnSubmitRoute = OfficeSuppliesAmountController.onSubmit(taxYear, businessId, NormalMode).url

  private val mockSessionRepository     = mock[SessionRepository]
  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(userType: UserType, form: Form[BigDecimal])

  private val userScenarios = Seq(
    UserScenario(userType = Individual, form = formProvider(Individual)),
    UserScenario(userType = Agent, form = formProvider(Agent))
  )

  private val someHttpError = ConnectorResponseError(HttpError(400, HttpErrorBody.SingleErrorBody("BAD_REQUEST", "some_reason")))

  private def buildApplication(userAnswers: Option[UserAnswers], userType: UserType): Application =
    applicationBuilder(userAnswers, userType)
      .overrides(bind[SelfEmploymentService].toInstance(mockSelfEmploymentService))
      .build()

  "OfficeSuppliesAmountController" - {
    userScenarios.foreach { userScenario =>
      s"when user is an ${userScenario.userType}" - {
        "when loading a page" - {
          "when an accounting type is returned by the service" - {
            "must return OK and the correct view" in {
              val application = buildApplication(Some(emptyUserAnswers), userScenario.userType)

              val view: OfficeSuppliesAmountView = application.injector.instanceOf[OfficeSuppliesAmountView]

              running(application) {
                when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
                val result                                                = route(application, request).value

                status(result) mustEqual OK

                contentAsString(result) mustEqual view(userScenario.form, NormalMode, userScenario.userType, accrual, taxYear, businessId)(
                  request,
                  messages(application)).toString
              }
            }

            "must populate the view correctly when the question has previously been answered" in {
              val userAnswers = UserAnswers(userAnswersId).set(OfficeSuppliesAmountPage, validAnswer, Some(businessId)).success.value

              val application = buildApplication(Some(userAnswers), userScenario.userType)

              val view: OfficeSuppliesAmountView = application.injector.instanceOf[OfficeSuppliesAmountView]

              running(application) {
                when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                val request = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
                val result  = route(application, request).value

                status(result) mustEqual OK

                contentAsString(result) mustEqual view(
                  userScenario.form.fill(validAnswer),
                  NormalMode,
                  userScenario.userType,
                  accrual,
                  taxYear,
                  businessId)(request, messages(application)).toString
              }
            }
          }
          "when no accounting type is returned by the service" - {
            "must redirect to the journey recovery controller" in {
              val application = buildApplication(Some(emptyUserAnswers), userScenario.userType)

              running(application) {
                when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Left(someHttpError))

                val request = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
                val result  = route(application, request).value

                status(result) mustEqual 303
              }
            }
          }

          "when submitting a page" - {
            "when an accounting type is returned by the service" - {
              "must redirect to the next page when valid data is submitted" in {
                when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
                when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                val application =
                  applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.userType)
                    .overrides(
                      bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
                      bind[SessionRepository].toInstance(mockSessionRepository),
                      bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
                    )
                    .build()

                running(application) {
                  val request = FakeRequest(POST, officeSuppliesAmountOnSubmitRoute).withFormUrlEncodedBody(("value", validAnswer.toString))
                  val result  = route(application, request).value

                  status(result) mustEqual SEE_OTHER
                  redirectLocation(result).value mustEqual onwardRoute.url
                }
              }

              "must return a Bad Request and errors when invalid data is submitted" in {
                val application = buildApplication(Some(emptyUserAnswers), userScenario.userType)

                val view: OfficeSuppliesAmountView = application.injector.instanceOf[OfficeSuppliesAmountView]

                running(application) {
                  when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                  val request = FakeRequest(POST, officeSuppliesAmountOnSubmitRoute).withFormUrlEncodedBody(("value", "invalid value"))

                  val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

                  val result = route(application, request).value

                  status(result) mustEqual BAD_REQUEST
                  contentAsString(result) mustEqual view(boundForm, NormalMode, userScenario.userType, accrual, taxYear, businessId)(
                    request,
                    messages(application)).toString
                }
              }
            }
            "when no accounting type is returned by the service" - {
              "must redirect to the journey recovery controller" in {
                val application = buildApplication(Some(emptyUserAnswers), userScenario.userType)

                running(application) {
                  when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Left(someHttpError))

                  val request = FakeRequest(POST, officeSuppliesAmountOnSubmitRoute).withFormUrlEncodedBody(("value", validAnswer.toString))
                  val result  = route(application, request).value

                  status(result) mustEqual 303
                }
              }
            }

          }
        }
      }
    }

  }

}
