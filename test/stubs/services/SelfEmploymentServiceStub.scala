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

import base.SpecBase._
import builders.BusinessDataBuilder.aBusinessData
import cats.data.EitherT
import models.Mode
import models.common._
import models.database.UserAnswers
import models.domain.{ApiResultT, BusinessData}
import models.errors.ServiceError
import models.journeys.{TaskList, TaskListWithRequest}
import models.requests.DataRequest
import pages.{OneQuestionPage, QuestionPage}
import play.api.libs.json.{Format, JsObject, Json, Writes}
import play.api.mvc.Result
import services.SelfEmploymentService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

case class SelfEmploymentServiceStub(
    getBusinessesResult: Either[ServiceError, Seq[BusinessData]] = Right(Seq(aBusinessData)),
    getBusinessResult: Either[ServiceError, BusinessData] = Right(aBusinessData),
    accountingType: Either[ServiceError, AccountingType] = Right(AccountingType.Accrual),
    saveAnswerResult: UserAnswers = UserAnswers("userId", JsObject.empty),
    getTaskList: Either[ServiceError, TaskListWithRequest] = Right(TaskListWithRequest(TaskList.empty, fakeOptionalRequest)),
    getJourneyStatusResult: Either[ServiceError, JourneyStatus] = Right(JourneyStatus.InProgress),
    setJourneyStatusResult: Either[ServiceError, Unit] = Right(()),
    getUserAnswersWithAccrual: UserAnswers = emptyUserAnswers.copy(data = Json.obj(businessId.value -> Json.obj("accountingType" -> "ACCRUAL")))
) extends SelfEmploymentService {

  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Seq[BusinessData]] =
    EitherT.fromEither[Future](getBusinessesResult)

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[BusinessData] =
    EitherT.fromEither[Future](getBusinessResult)

  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers] =
    Future.successful(saveAnswerResult)

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.pure[Future, ServiceError](())

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] =
    EitherT.fromEither[Future](getJourneyStatusResult)

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither[Future](setJourneyStatusResult)

  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(AccountingType, BusinessId)]): Future[UserAnswers] =
    Future(getUserAnswersWithAccrual)

  def submitBooleanAnswerAndClearDependentAnswers(pageUpdated: OneQuestionPage[Boolean],
                                                  businessId: BusinessId,
                                                  request: DataRequest[_],
                                                  newAnswer: Boolean): Future[UserAnswers] = ???

  def submitBooleanAnswerAndRedirect(pageUpdated: OneQuestionPage[Boolean],
                                     businessId: BusinessId,
                                     request: DataRequest[_],
                                     newAnswer: Boolean,
                                     taxYear: TaxYear,
                                     mode: Mode): Future[Result] = ???

  def persistAnswerAndRedirect[A: Writes](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          request: DataRequest[_],
                                          value: A,
                                          taxYear: TaxYear,
                                          mode: Mode): Future[Result] = ???
}
