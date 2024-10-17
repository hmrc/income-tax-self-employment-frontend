/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.nics

import base.{ControllerSpec, SpecBase}
import builders.BusinessDataBuilder._
import common.TestApp.buildAppFromUserType
import models.CheckMode
import models.common.BusinessId
import models.common.BusinessId.{classFourOtherExemption, nationalInsuranceContributions}
import models.database.UserAnswers
import pages.Page
import pages.nics.Class4NICsPage
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, status, writeableOf_AnyContentAsEmpty}
import stubs.services.SelfEmploymentServiceStub
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.Assertions.assertEqualWithDiff
import viewmodels.checkAnswers.BooleanSummary
import viewmodels.checkAnswers.nics.{
  Class4DivingExemptSummary,
  Class4ExemptionReasonSummary,
  Class4NonDivingExemptSingleBusinessSummary,
  Class4NonDivingExemptSummary
}
import views.html.standard.CheckYourAnswersView

class NICsCYAControllerSpec extends ControllerSpec {

  private val businesses    = Seq(aBusinessData, aBusinessDataCashAccounting)
  private val businessesIds = businesses.map(b => BusinessId(b.businessId)).toList

  def userAnswers: UserAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      BusinessId.nationalInsuranceContributions.value -> Json.obj(
        "class4NICs"            -> true,
        "class4ExemptionReason" -> "trusteeExecutorAdmin",
        "class4DivingExempt"    -> businessesIds,
        "class4NonDivingExempt" -> List(classFourOtherExemption)
      ))
  )

  def onPageLoad: String = routes.NICsCYAController.onPageLoad(taxYear).url

  def onPageLoadRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, onPageLoad)

  "onPageLoad" - {
    userTypeCases.foreach { userType =>
      s"when user is an $userType, should return Ok and render correct view" in {
        val stubService            = SelfEmploymentServiceStub(getBusinessesResult = Right(businesses))
        val application            = buildAppFromUserType(userType, Some(userAnswers), Some(stubService))
        implicit val msg: Messages = SpecBase.messages(application)

        val result = route(application, onPageLoadRequest).value
        val summaryList = SummaryList(
          rows = List(
            new BooleanSummary(Class4NICsPage, routes.Class4NICsController.onPageLoad(taxYear, CheckMode))
              .row(userAnswers, taxYear, nationalInsuranceContributions, userType, rightTextAlign = false),
            Class4ExemptionReasonSummary.row(userAnswers, userType, taxYear),
            Class4DivingExemptSummary.row(userAnswers, businesses, userType, taxYear),
            Class4NonDivingExemptSummary.row(userAnswers, businesses, userType, taxYear),
            Class4NonDivingExemptSingleBusinessSummary.row(userAnswers, userType, taxYear)
          ).flatten,
          classes = "govuk-!-margin-bottom-7"
        )

        val expectedView: String = {
          val view = application.injector.instanceOf[CheckYourAnswersView]
          view(Page.cyaCheckYourAnswersHeading, taxYear, userType, summaryList, routes.NICsCYAController.onSubmit(taxYear))(onPageLoadRequest, msg)
            .toString()
        }

        status(result) mustBe OK
        assertEqualWithDiff(contentAsString(result), expectedView)
      }
    }

  }

}
