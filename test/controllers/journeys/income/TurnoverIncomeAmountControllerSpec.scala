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
import controllers.journeys.income.routes.TurnoverIncomeAmountController
import controllers.standard.routes.JourneyRecoveryController
import forms.income.TurnoverIncomeAmountFormProvider
import models.mdtp.CashOrAccrual.isAccrual
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.income.TurnoverIncomeAmountPage
import play.api.data.Form
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SelfEmploymentService
import views.html.journeys.income.TurnoverIncomeAmountView

import scala.concurrent.Future

class TurnoverIncomeAmountControllerSpec extends SpecBase with MockitoSugar {

  val formProvider            = new TurnoverIncomeAmountFormProvider()
  val businessId              = "businessId"
  val validAnswer: BigDecimal = 100

  def onwardRoute = Call("GET", "/foo")

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  def turnoverIncomeAmountRoute(isPost: Boolean, mode: Mode): String =
    if (isPost) TurnoverIncomeAmountController.onSubmit(taxYear, mode).url
    else TurnoverIncomeAmountController.onPageLoad(taxYear, mode).url

  case class UserScenario(isWelsh: Boolean, isAgent: Boolean, form: Form[BigDecimal], accrualOrCash: String)

  val userScenarios = Seq(
    UserScenario(isWelsh = false, isAgent = false, formProvider("individual"), "ACCRUAL"),
    UserScenario(isWelsh = false, isAgent = true, formProvider("agent"), "CASH")
  )

  "TurnoverIncomeAmount Controller" - {

    "onPageLoad" - {

      userScenarios.foreach { userScenario =>
        s"when ${isWelshToString(userScenario.isWelsh)}, ${isAgentToString(userScenario.isAgent)} and has ${userScenario.accrualOrCash} type accounting" - {
          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accrualOrCash))
              val request = buildRequest(GET, turnoverIncomeAmountRoute(false, NormalMode), userScenario.isAgent)

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val view = application.injector.instanceOf[TurnoverIncomeAmountView]

              val expectedResult =
                view(userScenario.form, NormalMode, isAgentToString(userScenario.isAgent), taxYear, isAccrual(userScenario.accrualOrCash))(
                  request,
                  messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }

          "must populate the view correctly on a GET when the question has previously been answered" in {

            val userAnswers = UserAnswers(userAnswersId).set(TurnoverIncomeAmountPage, validAnswer).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers), userScenario.isAgent)
              .overrides(bind[SelfEmploymentService].toInstance(mockService))
              .build()
            implicit val messagesApi = application.injector.instanceOf[MessagesApi]

            running(application) {
              when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accrualOrCash))
              val request = buildRequest(GET, turnoverIncomeAmountRoute(false, CheckMode), userScenario.isAgent)

              val view = application.injector.instanceOf[TurnoverIncomeAmountView]

              val result = route(application, request).value

              val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

              val expectedResult = view(
                userScenario.form.fill(validAnswer),
                CheckMode,
                isAgentToString(userScenario.isAgent),
                taxYear,
                isAccrual(userScenario.accrualOrCash))(request, messages(application, userScenario.isWelsh)).toString

              status(result) mustEqual OK
              contentAsString(langResult) mustEqual expectedResult
            }
          }
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = buildRequest(GET, turnoverIncomeAmountRoute(false, NormalMode), false)

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
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SelfEmploymentService].toInstance(mockService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right("ACCRUAL"))

          val request =
            buildRequest(POST, turnoverIncomeAmountRoute(true, NormalMode), false)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      userScenarios.foreach { userScenario =>
        s"when ${isWelshToString(userScenario.isWelsh)}, ${isAgentToString(userScenario.isAgent)} and has ${userScenario.accrualOrCash} type accounting" - {
          "must return a Bad Request and errors when" - {
            "an empty form is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()
              implicit val messagesApi = application.injector.instanceOf[MessagesApi]

              running(application) {
                when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accrualOrCash))

                val request =
                  buildRequest(POST, turnoverIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                    .withFormUrlEncodedBody(("value", ""))

                val boundForm = userScenario.form.bind(Map("value" -> ""))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value

                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                val expectedResult =
                  view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear, isAccrual(userScenario.accrualOrCash))(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(langResult) mustEqual expectedResult
              }
            }

            "invalid data is submitted" in {

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = userScenario.isAgent)
                .overrides(bind[SelfEmploymentService].toInstance(mockService))
                .build()
              implicit val messagesApi = application.injector.instanceOf[MessagesApi]

              running(application) {
                when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accrualOrCash))

                val request =
                  buildRequest(POST, turnoverIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                    .withFormUrlEncodedBody(("value", "non-BigDecimal"))

                val boundForm = userScenario.form.bind(Map("value" -> "non-BigDecimal"))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value

                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                val expectedResult =
                  view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear, isAccrual(userScenario.accrualOrCash))(
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
                when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accrualOrCash))

                val request =
                  buildRequest(POST, turnoverIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                    .withFormUrlEncodedBody(("value", "-23"))

                val boundForm = userScenario.form.bind(Map("value" -> "-23"))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value

                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                val expectedResult =
                  view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear, isAccrual(userScenario.accrualOrCash))(
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
                when(mockService.getBusinessAccountingType(any, meq(businessId), any)(any)) thenReturn Future(Right(userScenario.accrualOrCash))

                val request =
                  buildRequest(POST, turnoverIncomeAmountRoute(true, NormalMode), userScenario.isAgent)
                    .withFormUrlEncodedBody(("value", "100000000000.01"))

                val boundForm = userScenario.form.bind(Map("value" -> "100000000000.01"))

                val view = application.injector.instanceOf[TurnoverIncomeAmountView]

                val result = route(application, request).value

                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                val expectedResult =
                  view(boundForm, NormalMode, isAgentToString(userScenario.isAgent), taxYear, isAccrual(userScenario.accrualOrCash))(
                    request,
                    messages(application, userScenario.isWelsh)).toString

                status(result) mustEqual BAD_REQUEST
                contentAsString(langResult) mustEqual expectedResult
              }
            }
          }
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" ignore { // TODO unignore when RequireData is implemented

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            buildRequest(POST, turnoverIncomeAmountRoute(true, NormalMode), false)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

}
