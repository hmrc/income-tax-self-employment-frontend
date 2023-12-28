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

import cats.data.EitherT
import models.common._
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.ServiceError
import models.journeys.{Journey, TaskList}
import pages.QuestionPage
import play.api.libs.json.{Format, JsObject, Writes}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future
import models.journeys.TaskListWithRequest
import models.requests.OptionalDataRequest
import play.api.mvc.AnyContent
import base.SpecBase._

case class SelfEmploymentServiceStub(
    accountingType: Either[ServiceError, AccountingType] = Right(AccountingType.Cash),
    saveAnswerResult: UserAnswers = UserAnswers("userId", JsObject.empty),
    submittedAnswers: Either[ServiceError, Option[JsObject]] = Right(Some(JsObject.empty)),
    getTaskList: Either[ServiceError, TaskListWithRequest] = Right(TaskListWithRequest(TaskList.empty, fakeOptionalRequest)),
    getJourneyStatusResult: Either[ServiceError, JourneyStatus] = Right(JourneyStatus.InProgress),
    setJourneyStatusResult: Either[ServiceError, Unit] = Right(())
) extends SelfEmploymentServiceBase {

  def getTaskList(taxYear: TaxYear, request: OptionalDataRequest[AnyContent])(implicit hc: HeaderCarrier): ApiResultT[TaskListWithRequest] =
    EitherT.fromEither[Future](getTaskList)

  def getJourneyStatus(journey: Journey, nino: Nino, taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] = ???

  def getAccountingType(nino: String, businessId: BusinessId, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, String]] =
    Future.successful(accountingType.map(_.entryName))

  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers] =
    Future.successful(saveAnswerResult)

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = ???

  def getSubmittedAnswers[A: Format](context: JourneyContext)(implicit hc: HeaderCarrier): ApiResultT[Option[A]] =
    EitherT(Future.successful(submittedAnswers.asInstanceOf[Either[ServiceError, Option[A]]]))

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] =
    EitherT.fromEither[Future](getJourneyStatusResult)

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither[Future](setJourneyStatusResult)

}
