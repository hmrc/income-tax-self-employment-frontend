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

package controllers.journeys.expenses.staffCosts

import base.SpecBase._
import builders.UserBuilder.aNoddyUser
import common.TestApp.buildAppFromUserAnswers
import models.common.{Language, UserType, onwardRoute}
import models.database.UserAnswers
import models.requests.DataRequest
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import pages.expenses.staffCosts.{StaffCostsAmountPage, StaffCostsDisallowableAmountPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.expenses.staffCosts._
import views.html.journeys.expenses.staffCosts.StaffCostsCYAView

import scala.concurrent.Future

class StaffCostsCYAControllerSpec extends AnyWordSpecLike with Matchers with TableDrivenPropertyChecks {

  private def createUserAnswers(amount: BigDecimal, disallowableAmount: Option[BigDecimal]): UserAnswers = {
    val answers = emptyUserAnswers.set(StaffCostsAmountPage, amount, Some(businessId)).success.value
    disallowableAmount match {
      case Some(disAmount) => answers.set(StaffCostsDisallowableAmountPage, disAmount, Some(businessId)).success.value
      case _               => answers
    }
  }

  lazy val routeUnderTest: String                          = routes.StaffCostsCYAController.onPageLoad(taxYear, businessId).url
  lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routeUnderTest)

  "onPageLoad" should {
    val cases = Table(
      ("tailoring", "amount", "disallowable"),
      ("no", BigDecimal(100.0), None),
      ("yes", BigDecimal(100.00), None),
      ("yes", BigDecimal(100.00), Some(BigDecimal(50.00)))
    )

    forAll(cases) { case (tailoring, amount, disallowable) =>
      s"return OK and render view for $tailoring, amount=$amount, and disallowable=$disallowable" in {

        val (application, userAnswers, result) = callRoute(createUserAnswers(amount, disallowable))

        status(result) mustBe OK

        implicit val msg: Messages = messages(application, Language.English)
        val dataRequest            = DataRequest(getRequest, userAnswersId, aNoddyUser, userAnswers)
        val expectedRows = List(
          StaffCostsAmountSummary.row(dataRequest, taxYear, businessId),
          StaffCostsDisallowableAmountSummary.row(dataRequest.userAnswers, taxYear, businessId, dataRequest.userType)
        ).flatten
        contentAsString(result) mustEqual createExpectedView(application, expectedRows)
      }
    }
  }

  def callRoute(userAnswers: UserAnswers): (Application, UserAnswers, Future[Result]) = {
    val application = buildAppFromUserAnswers(userAnswers)

    (application, userAnswers, route(application, getRequest).value)
  }

  def createExpectedView(application: Application, expectedRows: List[SummaryListRow])(implicit msg: Messages): String = {
    val view        = application.injector.instanceOf[StaffCostsCYAView]
    val summaryList = SummaryList(expectedRows)
    val nextRoute   = onwardRoute.url
    view(taxYear, UserType.Individual, summaryList, nextRoute)(getRequest, msg).toString()
  }

}
