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

package controllers.journeys.expenses.tailoring

import base.{CYAControllerBaseSpec, CYAOnPageLoadControllerBaseSpec}
import builders.ExpensesTailoringJsonBuilder._
import cats.data.EitherT
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import controllers.journeys.expenses.tailoring
import controllers.{journeys, standard}
import models.NormalMode
import models.common.UserType.Individual
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContentAsEmpty, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, defaultAwaitTimeout, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.tailoring.buildTailoringSummaryList
import views.html.standard.CheckYourAnswersView

import scala.concurrent.Future

class ExpensesTailoringCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAControllerBaseSpec {

  override val pageHeading: String = ExpensesTailoringCYAPage.toString
  val journey: Journey             = Journey.ExpensesTailoring

  def onPageLoadCall: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = tailoring.routes.ExpensesTailoringCYAController.onSubmit

  def expectedSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList =
    buildTailoringSummaryList(userAnswers, taxYear, businessId, userType)

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_]): String = {
    val view         = application.injector.instanceOf[CheckYourAnswersView]
    val categoryText = summaryList.rows.head.value.content
    val optCategory  = if (categoryText == Text(messages(s"expenses.$IndividualCategories"))) "Categories" else ""
    val heading      = s"$pageHeading$optCategory"
    view(heading, taxYear, userType, summaryList, onSubmitCall(taxYear, businessId))(request, messages).toString()
  }

  override val testDataCases: List[JsObject] =
    List(
      allYesIndividualCategoriesAnswers,
      allNoIndividualCategoriesAnswers,
      mixedIndividualCategoriesAnswers,
      noExpensesAnswers
    )

  private val mockService: SelfEmploymentService = mock[SelfEmploymentService]
  private val mockSessionRepository              = mock[SessionRepository]

  implicit lazy val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, onSubmitCall(taxYear, businessId).url)

  private def createApp(userType: UserType, answers: Option[UserAnswers], mockSessionRepository: SessionRepository): Application = {
    val overrideBindings: List[Binding[_]] = bind[SessionRepository].toInstance(mockSessionRepository) :: bindings

    applicationBuilder(answers, isAgent(userType.toString))
      .overrides(overrideBindings)
      .build()
  }
  override val bindings: List[Binding[_]] = List(bind[SelfEmploymentService].toInstance(mockService))

  "submitting a page" - {
//    testDataCases.head { data =>

    "journey answers submitted successfully" - {
      "redirect to section completed" in {
        mockService.submitAnswers(*, *)(*, *) returns EitherT(Future.successful(().asRight))
        val application            = createApp(Individual, buildUserAnswers(testDataCases.head).some, mockSessionRepository)
        val result: Future[Result] = route(application, postRequest).value

        status(result) shouldBe 303

        redirectLocation(result).value shouldBe journeys.routes.SectionCompletedStateController
          .onPageLoad(taxYear, businessId, journey.toString, NormalMode)
          .url
      }
    }

    "an error occurred during answer submission" - {
      "redirect to journey recovery" in {
        mockService.submitAnswers(*, *)(*, *) returns EitherT(Future.successful(httpError.asLeft))

        val application            = createApp(Individual, buildUserAnswers(testDataCases.head).some, mockSessionRepository)
        val result: Future[Result] = route(application, postRequest).value

        status(result) shouldBe 303
        redirectLocation(result).value shouldBe standard.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
//    }
  }

}
