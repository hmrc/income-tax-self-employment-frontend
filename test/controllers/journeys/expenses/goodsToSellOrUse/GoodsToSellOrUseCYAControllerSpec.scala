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

package controllers.journeys.expenses.goodsToSellOrUse

import base.CYAControllerBaseSpec
import builders.UserBuilder
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import controllers.journeys.routes._
import controllers.standard.routes._
import models.NormalMode
import models.common._
import models.database.UserAnswers
import models.errors.HttpError
import models.errors.HttpErrorBody.SingleErrorBody
import models.journeys.Journey.ExpensesGoodsToSellOrUse
import models.journeys.expenses.ExpensesData
import models.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.http.Status.BAD_REQUEST
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, defaultAwaitTimeout, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import services.journeys.expenses.ExpensesService
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.goodsToSellOrUse.{DisallowableGoodsToSellOrUseAmountSummary, GoodsToSellOrUseAmountSummary}
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseCYAView

import scala.concurrent.Future

class GoodsToSellOrUseCYAControllerSpec extends CYAControllerBaseSpec("GoodsToSellOrUseCYAController") with ArgumentMatchersSugar {
  private val mockExpensesService = mock[ExpensesService]

  override val bindings: List[Binding[_]] =
    List(bind[ExpensesNavigator].to(new FakeExpensesNavigator(onwardRoute)), bind[ExpensesService].toInstance(mockExpensesService))

  override protected lazy val onPageLoadRoute: String = routes.GoodsToSellOrUseCYAController.onPageLoad(taxYear, businessId).url

  private val userAnswerData = Json
    .parse(s"""
       |{
       |  "$businessId": {
       |    "goodsToSellOrUse": "yesDisallowable",
       |    "goodsToSellOrUseAmount": 100.00,
       |    "disallowableGoodsToSellOrUseAmount": 100.00
       |  }
       |}
       |""".stripMargin)
    .as[JsObject]

  override protected val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)

  override def expectedSummaryList(user: UserType)(implicit messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      GoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, user.toString).value,
      DisallowableGoodsToSellOrUseAmountSummary.row(userAnswers, taxYear, businessId, user.toString).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override def expectedView(scenario: TestScenario, summaryList: SummaryList, nextRoute: String)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[GoodsToSellOrUseCYAView]
    view(taxYear, businessId, scenario.userType.toString, summaryList).toString()
  }

  private val goodsToSellJourneyAnswers =
    GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount = 100.00, disallowableGoodsToSellOrUseAmount = Some(100.00))

  private val httpError = HttpError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR"))

  // TODO pull this into a base test class once we have additional CYA controllers persisting.
  "GoodsToSellOrUseCYAController" - {
    "on page submission" - {
      "goods to sell journey answers are submitted successfully" - {
        "redirect to the section completed controller" in new TestScenario(UserType.Individual, userAnswers.some) {
          lazy val onSubmitPath: String = routes.GoodsToSellOrUseCYAController.onSubmit(taxYear, businessId).url
          val expensesData: ExpensesData =
            ExpensesData(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, ExpensesGoodsToSellOrUse, UserBuilder.aNoddyUser.mtditid)

          mockExpensesService
            .sendExpensesAnswers(eqTo(expensesData), eqTo(goodsToSellJourneyAnswers))(*, *, *) returns Future
            .successful(().asRight)

          val result: Future[Result] = route(application, postRequestWithPath(onSubmitPath)).value

          status(result) shouldBe 303
          redirectLocation(result).value shouldBe SectionCompletedStateController
            .onPageLoad(taxYear, businessId, ExpensesGoodsToSellOrUse.toString, NormalMode)
            .url
        }
      }
      // Stand-in test until unhappy path ticket is picked up.
      "goods to sell journey answers unsuccessfully submitted" - {
        "redirect to the Journey Recovery controller" in new TestScenario(UserType.Individual, userAnswers.some) {
          lazy val onSubmitPath: String = routes.GoodsToSellOrUseCYAController.onSubmit(taxYear, businessId).url
          val expensesData: ExpensesData =
            ExpensesData(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, ExpensesGoodsToSellOrUse, UserBuilder.aNoddyUser.mtditid)

          mockExpensesService
            .sendExpensesAnswers(eqTo(expensesData), eqTo(goodsToSellJourneyAnswers))(*, *, *) returns Future
            .successful(httpError.asLeft)

          val result: Future[Result] = route(application, postRequestWithPath(onSubmitPath)).value

          status(result) shouldBe 303
          redirectLocation(result).value shouldBe JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
  private lazy val postRequestWithPath = (route: String) => FakeRequest(POST, route)

}
