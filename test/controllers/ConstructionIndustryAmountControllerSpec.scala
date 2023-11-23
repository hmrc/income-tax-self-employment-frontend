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
import forms.ConstructionIndustryAmountFormProvider
import controllers.journeys.expenses.construction.routes.ConstructionIndustryAmountController
import models.NormalMode
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ConstructionIndustryAmountPage
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
import views.html.ConstructionIndustryAmountView
import scala.concurrent.Future

class ConstructionIndustryAmountControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new ConstructionIndustryAmountFormProvider()
  private val validAnswer  = BigDecimal(100.00)

  private val onwardRoute = Call("GET", "/foo")
  private lazy val constructionIndustryAmountPageLoadRoute = ConstructionIndustryAmountController.onPageLoad(taxYear, stubbedBusinessId, NormalMode).url
  private lazy val constructionIndustryAmountOnSubmitRoute = ConstructionIndustryAmountController.onSubmit(taxYear, stubbedBusinessId, NormalMode).url

  private val mockSessionRepository = mock[SessionRepository]
  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  case class UserScenario(isWelsh: Boolean, authUser: String, form: Form[BigDecimal])

  private val userScenarios = Seq(
    UserScenario(isWelsh = false, authUser = individual, form = formProvider(individual)),
    UserScenario(isWelsh = false, authUser = agent, form = formProvider(agent))
  )

  private def buildApplication(userAnswers: Option[UserAnswers], authUser: String): Application = {
    val isAgent = authUser match {
      case "individual" => false
      case "agent" => true
    }
    applicationBuilder(userAnswers, isAgent)
      .overrides(bind[SelfEmploymentService].toInstance(mockSelfEmploymentService))
      .build()
  }

  "ConstructionIndustryAmount Controller" - {
    userScenarios.foreach { userScenario =>
      s"when language is ${getLanguage(userScenario.isWelsh)}, user is an ${userScenario.authUser}" - {
        "when loading a page" - {
          "when an accounting type is returned by the service" - {
            "must return OK and the correct view" in {
              val application = buildApplication(Some(emptyUserAnswers), userScenario.authUser)

              implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
              val view: ConstructionIndustryAmountView = application.injector.instanceOf[ConstructionIndustryAmountView]

              running(application) {

                implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, constructionIndustryAmountPageLoadRoute)
                val result = route(application, request).value
                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                status(langResult) mustEqual OK

                contentAsString(langResult) mustEqual view(userScenario.form, NormalMode, userScenario.authUser, taxYear, stubbedBusinessId)(
                  request,
                  messages(application)).toString
              }
            }

            "must populate the view correctly when the question has previously been answered" in {
              val userAnswers = UserAnswers(userAnswersId).set(ConstructionIndustryAmountPage, validAnswer, Some(stubbedBusinessId)).success.value

              val application = buildApplication(Some(userAnswers), userScenario.authUser)

              implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
              val view: ConstructionIndustryAmountView = application.injector.instanceOf[ConstructionIndustryAmountView]

              running(application) {

                val request = FakeRequest(GET, constructionIndustryAmountPageLoadRoute)
                val result = route(application, request).value
                val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                status(langResult) mustEqual OK

                contentAsString(langResult) mustEqual view(
                  userScenario.form.fill(validAnswer),
                  NormalMode,
                  userScenario.authUser,
                  taxYear,
                  stubbedBusinessId)(request, messages(application)).toString
              }
            }
          }

          "when submitting a page" - {
            "when an accounting type is returned by the service" - {
              "must redirect to the next page when valid data is submitted" in {
                when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
                when(mockSelfEmploymentService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right(accrual))

                val application =
                  applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent(userScenario.authUser))
                    .overrides(
                      bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
                      bind[SessionRepository].toInstance(mockSessionRepository),
                      bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
                    )
                    .build()

                running(application) {
                  val request = FakeRequest(POST, constructionIndustryAmountOnSubmitRoute).withFormUrlEncodedBody(("value", validAnswer.toString))
                  val result = route(application, request).value

                  status(result) mustEqual SEE_OTHER
                  redirectLocation(result).value mustEqual onwardRoute.url
                }
              }

              "must return a Bad Request and errors when invalid data is submitted" in {
                val application = buildApplication(Some(emptyUserAnswers), userScenario.authUser)

                val view: ConstructionIndustryAmountView = application.injector.instanceOf[ConstructionIndustryAmountView]
                implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

                running(application) {
                  when(mockSelfEmploymentService.getAccountingType(any, meq(stubbedBusinessId), any)(any)) thenReturn Future(Right(accrual))

                  val request = FakeRequest(POST, constructionIndustryAmountOnSubmitRoute).withFormUrlEncodedBody(("value", "invalid value"))

                  val boundForm = userScenario.form.bind(Map("value" -> "invalid value"))

                  val result = route(application, request).value
                  val langResult = if (userScenario.isWelsh) result.map(_.withLang(cyLang)) else result

                  status(langResult) mustEqual BAD_REQUEST
                  contentAsString(langResult) mustEqual view(boundForm, NormalMode, userScenario.authUser, taxYear, stubbedBusinessId)(
                    request,
                    messages(application)).toString
                }
              }
            }

          }
        }
      }
    }
  }
}
