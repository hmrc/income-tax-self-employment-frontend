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

package models.journeys.nics

import controllers.standard
import models.common.BusinessId
import models.database.UserAnswers
import pages.nics._
import play.api.libs.json.{Format, Json}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

case class NICsJourneyAnswers(class2Answers: Option[NICsClass2Answers], class4Answers: Option[NICsClass4Answers]) {
  def isClass4: Boolean = class4Answers.isDefined
}

object NICsJourneyAnswers {
  implicit val formats: Format[NICsJourneyAnswers] = Json.format[NICsJourneyAnswers]

  def buildFromUserAnswers(userAnswers: UserAnswers): Either[Result, NICsJourneyAnswers] = {
    val class2         = userAnswers.get(Class2NICsPage, BusinessId.nationalInsuranceContributions)
    val class4         = userAnswers.get(Class4NICsPage, BusinessId.nationalInsuranceContributions)
    val class4Reason   = userAnswers.get(Class4ExemptionReasonPage, BusinessId.nationalInsuranceContributions)
    val class4Diver    = userAnswers.get(Class4DivingExemptPage, BusinessId.nationalInsuranceContributions)
    val class4NonDiver = userAnswers.get(Class4NonDivingExemptPage, BusinessId.nationalInsuranceContributions)
    (class2, class4) match {
      case (Some(class2), None) => Right(NICsJourneyAnswers(Some(NICsClass2Answers(class2)), None))
      case (None, Some(class4)) => Right(NICsJourneyAnswers(None, Some(NICsClass4Answers(class4, class4Reason, class4Diver, class4NonDiver))))
      case _                    => Left(Redirect(standard.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
