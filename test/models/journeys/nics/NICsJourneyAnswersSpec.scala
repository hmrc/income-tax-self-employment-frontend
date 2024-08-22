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

package models.journeys.nics

import base.SpecBase.userAnswersId
import cats.implicits.catsSyntaxEitherId
import controllers.standard
import models.common.BusinessId
import models.database.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect

class NICsJourneyAnswersSpec extends AnyFreeSpec with ScalaCheckPropertyChecks {

  private val businessId = BusinessId.nationalInsuranceContributions.value
  private val testScenarios = Table(
    ("userAnswersData", "expectedNicsJourneyAnswers"),
    (Json.obj("class2NICs" -> true), NICsJourneyAnswers(Some(NICsClass2Answers(true)), None)),
    (Json.obj("class2NICs" -> false), NICsJourneyAnswers(Some(NICsClass2Answers(false)), None)),
    (Json.obj("class4NICs" -> false), NICsJourneyAnswers(None, Some(NICsClass4Answers(false, None, None, None)))),
    (
      Json.obj("class4NICs" -> true, "class4ExemptionReason" -> "trusteeExecutorAdmin"),
      NICsJourneyAnswers(None, Some(NICsClass4Answers(true, Some(ExemptionReason.TrusteeExecutorAdmin), None, None)))),
    (
      Json.obj("class4NICs" -> true, "class4DivingExempt" -> List("businessID")),
      NICsJourneyAnswers(None, Some(NICsClass4Answers(true, None, Some(List(BusinessId("businessID"))), None))))
  )

  "buildFromUserAnswers" - {
    "must build the correct Class 2 or 4 NICsJourneyAnswers to send downstream from UserAnswers" in {
      forAll(testScenarios) { case (userAnswersData, expectedNicsJourneyAnswers) =>
        val userAnswers = UserAnswers(userAnswersId, Json.obj(businessId -> userAnswersData))
        val result      = NICsJourneyAnswers.buildFromUserAnswers(userAnswers)

        assert(result === expectedNicsJourneyAnswers.asRight)
      }
    }
    "should return a Left(errorRedirect) when" - {
      val errorRedirect = Left(Redirect(standard.routes.JourneyRecoveryController.onPageLoad()))
      "there are class 2 AND class 4 answers" in {
        val userAnswersData = Json.obj("class2NICs" -> false, "class4NICs" -> false)
        val result          = NICsJourneyAnswers.buildFromUserAnswers(UserAnswers(userAnswersId, Json.obj(businessId -> userAnswersData)))

        assert(result === errorRedirect)
      }
      "there are neither class 2 nor class 4 answers" in {
        val userAnswersData = Json.obj()
        val result          = NICsJourneyAnswers.buildFromUserAnswers(UserAnswers(userAnswersId, Json.obj(businessId -> userAnswersData)))

        assert(result === errorRedirect)
      }
    }
  }
}
