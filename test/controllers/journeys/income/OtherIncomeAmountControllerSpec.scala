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

package controllers.journeys.income

import base.SpecBase
import controllers.journeys.income.routes.{OtherIncomeAmountController, TradingAllowanceController, TurnoverNotTaxableController}
import controllers.standard.routes.JourneyRecoveryController
import forms.income.OtherIncomeAmountFormProvider
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.OtherIncomeAmountPage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.income.OtherIncomeAmountView

import scala.concurrent.Future

class OtherIncomeAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new OtherIncomeAmountFormProvider()
  val validAnswer: BigDecimal = 100.00
  val turnoverAmount          = 1000.00
  val businessId              = "businessId"
  val turnoverNotTaxableCall  = TurnoverNotTaxableController.onPageLoad(taxYear, NormalMode)
  val tradingAllowanceCall    = TradingAllowanceController.onPageLoad(taxYear, NormalMode)

  val mockService: SelfEmploymentService       = mock[SelfEmploymentService]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  def otherIncomeAmountRoute(isPost: Boolean, mode: Mode): String =
    if (isPost) OtherIncomeAmountController.onSubmit(taxYear, mode).url
    else OtherIncomeAmountController.onPageLoad(taxYear, mode).url

  def onwardRoute(isAccrual: Boolean): Call =
    if (isAccrual) turnoverNotTaxableCall else tradingAllowanceCall

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[BigDecimal])

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formProvider("individual")),
    UserScenario(isWelsh = false, isAgent = true, formProvider("agent"))
  )

  "OtherIncomeAmount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when language is ${isWelshToString(userScenario.isWelsh)} and user is an ${isAgentToString(userScenario.isAgent)}" - {
          "must return OK and the correct view for a GET" in {

            val application          = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = buildRequest(GET, otherIncomeAmountRoute(false, NormalMode), userScenario.isAgent)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(OtherIncomeAmountPage, validAnswer).success.value

            val application          = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent).build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              val request = buildRequest(GET, otherIncomeAmountRoute(false, CheckMode), userScenario.isAgent)

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(userScenario.form.fill(validAnswer), CheckMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = buildRequest(GET, otherIncomeAmountRoute(false, NormalMode), false)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "when valid data is submitted must redirect to the" - {
        "TurnoverNotTaxable page when accounting type is 'ACCRUAL'" in {

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute(isAccrual = true))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
            when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))

            val request =
              buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), false)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual turnoverNotTaxableCall.url
          }
        }

        "TradingAllowance page when accounting type is 'CASH'" in {

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute(isAccrual = false))),
                bind[SelfEmploymentService].toInstance(mockService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
            when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("CASH"))

            val request =
              buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), false)
                .withFormUrlEncodedBody(("value", validAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual tradingAllowanceCall.url
          }
        }
      }

      userScenarios.foreach { userScenario =>
        s"when language is ${isWelshToString(userScenario.isWelsh)} and user is an ${isAgentToString(userScenario.isAgent)}" - {
          "must return a Bad Request and errors when an empty form is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))
              val request =
                buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = userScenario.form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

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
              when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))
              val request =
                buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                  .withFormUrlEncodedBody(("value", "non-BigDecimal"))

              val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "a negative number is submitted" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))
              val request =
                buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                  .withFormUrlEncodedBody(("value", "-23"))

              val boundForm = userScenario.form.bind(Map("value" -> "-23"))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "turnover income amount exceeds £100,000,000,000.00" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))
              val request =
                buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                  .withFormUrlEncodedBody(("value", "100000000000.01"))

              val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

              val view = application.injector.instanceOf[OtherIncomeAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear)(
                request,
                messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual BAD_REQUEST
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            buildRequest(POST, otherIncomeAmountRoute(true, NormalMode), false)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
