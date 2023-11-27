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

package stubs.services

import connectors.httpParser.GetTradesStatusHttpParser.GetTradesStatusResponse
import models.common.{AccountingType, BusinessId, TaxYear}
import models.database.UserAnswers
import models.errors.HttpError
import pages.QuestionPage
import play.api.libs.json.Writes
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

case class SelfEmploymentServiceStub(
    accountingType: Either[HttpError, AccountingType],
    saveAnswerResult: UserAnswers
) extends SelfEmploymentServiceBase {

  def getCompletedTradeDetails(nino: String, taxYear: TaxYear, mtditid: String)(implicit hc: HeaderCarrier): Future[GetTradesStatusResponse] = ???

  def getAccountingType(nino: String, businessId: String, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[HttpError, String]] =
    Future.successful(accountingType.map(_.entryName))

  def saveAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers] =
    Future.successful(saveAnswerResult)

}
