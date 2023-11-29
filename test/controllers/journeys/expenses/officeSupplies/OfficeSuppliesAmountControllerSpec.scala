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
import models.database.UserAnswers
import models.errors.{HttpError, HttpErrorBody}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.expenses.officeSupplies.OfficeSuppliesAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
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

  case class UserScenario(isWelsh: Boolean, authUser: String, form: Form[BigDecimal])

  private val userScenarios = Seq(
    UserScenario(isWelsh = false, authUser = individual, form = formProvider(individual)),
    UserScenario(isWelsh = false, authUser = agent, form = formProvider(agent))
  )

  private val someHttpError = HttpError(400, HttpErrorBody.SingleErrorBody("BAD_REQUEST", "some_reason"))

  private def buildApplication(userAnswers: Option[UserAnswers], authUser: String): Application = {
    val isAgent = authUser match {
      case "individual" => false
      case "agent"      => true
    }
    applicationBuilder(userAnswers, isAgent)
      .overrides(bind[SelfEmploymentService].toInstance(mockSelfEmploymentService))
      .build()
  }

  "OfficeSuppliesAmountController" - {
    userScenarios.foreach { userScenario =>
      s"when language is ${getLanguage(userScenario.isWelsh)}, user is an ${userScenario.authUser}" - {
        "when loading a page" - {
          "when an accounting type is returned by the service" - {
            "must return OK and the correct view" in {
              val application = buildApplication(Some(emptyUserAnswers), userScenario.authUser)

              implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
              val view: OfficeSuppliesAmountView    = application.injector.instanceOf[OfficeSuppliesAmountView]

              running(application) {
                when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
                val result                                                = route(application, request).value
                val langResult                                            = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                status(langResult) mustEqual OK

                contentAsString(langResult) mustEqual view(userScenario.form, NormalMode, userScenario.authUser, accrual, taxYear, businessId)(
                  request,
                  messages(application)).toString
              }
            }

            "must populate the view correctly when the question has previously been answered" in {
              val userAnswers = UserAnswers(userAnswersId).set(OfficeSuppliesAmountPage, validAnswer, Some(businessId)).success.value

              val application = buildApplication(Some(userAnswers), userScenario.authUser)

              implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
              val view: OfficeSuppliesAmountView    = application.injector.instanceOf[OfficeSuppliesAmountView]

              running(application) {
                when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                val request    = FakeRequest(GET, officeSuppliesAmountPageLoadRoute)
                val result     = route(application, request).value
                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                status(langResult) mustEqual OK

                contentAsString(langResult) mustEqual view(
                  userScenario.form.fill(validAnswer),
                  NormalMode,
                  userScenario.authUser,
                  accrual,
                  taxYear,
                  businessId)(request, messages(application)).toString
              }
            }
          }
          "when no accounting type is returned by the service" - {
            "must redirect to the journey recovery controller" in {
              val application = buildApplication(Some(emptyUserAnswers), userScenario.authUser)

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
                  applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.authUser))
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
                val application = buildApplication(Some(emptyUserAnswers), userScenario.authUser)

                val view: OfficeSuppliesAmountView    = application.injector.instanceOf[OfficeSuppliesAmountView]
                implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

                running(application) {
                  when(mockSelfEmploymentService.getAccountingType(any, anyBusinessId, any)(any)) thenReturn Future(Right(accrual))

                  val request = FakeRequest(POST, officeSuppliesAmountOnSubmitRoute).withFormUrlEncodedBody(("value", "invalid value"))

                  val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

                  val result     = route(application, request).value
                  val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                  status(langResult) mustEqual BAD_REQUEST
                  contentAsString(langResult) mustEqual view(boundForm, NormalMode, userScenario.authUser, accrual, taxYear, businessId)(
                    request,
                    messages(application)).toString
                }
              }
            }
            "when no accounting type is returned by the service" - {
              "must redirect to the journey recovery controller" in {
                val application = buildApplication(Some(emptyUserAnswers), userScenario.authUser)

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
