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
import builders.BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummary
import builders.UserBuilder.aUserDateOfBirth
import cats.data.EitherT
import models.Mode
import models.common._
import models.database.UserAnswers
import models.domain.{ApiResultT, BusinessData, BusinessIncomeSourcesSummary}
import models.errors.ServiceError
import models.journeys.nics.TaxableProfitAndLoss
import models.journeys.{TaskList, TaskListWithRequest}
import models.requests.DataRequest
import pages.{OneQuestionPage, QuestionPage}
import play.api.data.{Form, FormBinding}
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import services.SelfEmploymentService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

case class SelfEmploymentServiceStub(
    getBusinessesResult: Either[ServiceError, Seq[BusinessData]] = Right(Seq(aBusinessData)),
    getBusinessResult: Either[ServiceError, BusinessData] = Right(aBusinessData),
    accountingType: Either[ServiceError, AccountingType] = Right(AccountingType.Accrual),
    saveAnswerResult: UserAnswers = UserAnswers("userId", JsObject.empty),
    submitAnswersResult: Either[ServiceError, Unit] = Right(()),
    getTaskList: Either[ServiceError, TaskListWithRequest] = Right(TaskListWithRequest(TaskList.empty, fakeOptionalRequest)),
    getJourneyStatusResult: Either[ServiceError, JourneyStatus] = Right(JourneyStatus.InProgress),
    setJourneyStatusResult: Either[ServiceError, Unit] = Right(()),
    getUserAnswersWithAccrual: UserAnswers = emptyUserAnswers.copy(data = Json.obj(businessId.value -> Json.obj("accountingType" -> "ACCRUAL"))),
    getUserAnswersWithClearedData: UserAnswers = emptyUserAnswers,
    submitAnswerAndRedirectResult: Result = Redirect(onwardRoute),
    getUserDateOfBirthResult: Either[ServiceError, LocalDate] = Right(aUserDateOfBirth),
    getAllBusinessesTaxableProfitAndLossResult: Either[ServiceError, List[TaxableProfitAndLoss]] = Right(List.empty[TaxableProfitAndLoss]),
    getBusinessIncomeSourcesSummaryResult: Either[ServiceError, BusinessIncomeSourcesSummary] = Right(aBusinessIncomeSourcesSummary),
    getTotalTurnoverResult: Either[ServiceError, BigDecimal] = Right(BigDecimal(0)),
    clearSimplifiedExpensesDataResult: Either[ServiceError, UserAnswers] = Right(buildUserAnswers(JsObject.empty)))
    extends SelfEmploymentService {

  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Seq[BusinessData]] =
    EitherT.fromEither[Future](getBusinessesResult)

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[BusinessData] =
    EitherT.fromEither[Future](getBusinessResult)

  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers] =
    Future.successful(saveAnswerResult)

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext,
                                             userAnswers: UserAnswers,
                                             declareJourneyAnswers: Option[SubsetOfAnswers] = None)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither[Future](submitAnswersResult)

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] =
    EitherT.fromEither[Future](getJourneyStatusResult)

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither[Future](setJourneyStatusResult)

  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(TradingName, AccountingType, BusinessId)]): Future[UserAnswers] =
    Future(getUserAnswersWithAccrual)

  def submitGatewayQuestionAndClearDependentAnswers[A](pageUpdated: OneQuestionPage[A],
                                                       businessId: BusinessId,
                                                       userAnswers: UserAnswers,
                                                       newAnswer: A)(implicit reads: Reads[A], writes: Writes[A]): Future[UserAnswers] =
    Future(getUserAnswersWithClearedData)

  def submitGatewayQuestionAndRedirect[A](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          userAnswers: UserAnswers,
                                          newAnswer: A,
                                          taxYear: TaxYear,
                                          mode: Mode)(implicit reads: Reads[A], writes: Writes[A]): Future[Result] =
    Future(submitAnswerAndRedirectResult)

  def persistAnswerAndRedirect[A: Writes](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          request: DataRequest[_],
                                          value: A,
                                          taxYear: TaxYear,
                                          mode: Mode): Future[Result] =
    Future(submitAnswerAndRedirectResult)
  def handleForm[A](form: Form[A], handleError: Form[_] => Result, handleSuccess: A => Future[Result])(implicit
      request: DataRequest[_],
      defaultFormBinding: FormBinding): Future[Result] =
    Future(submitAnswerAndRedirectResult)
  def defaultHandleForm[A](
      form: Form[A],
      page: OneQuestionPage[A],
      businessId: BusinessId,
      taxYear: TaxYear,
      mode: Mode,
      handleError: Form[_] => Result)(implicit request: DataRequest[_], defaultFormBinding: FormBinding, writes: Writes[A]): Future[Result] =
    Future(submitAnswerAndRedirectResult)

  def getUserDateOfBirth(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[LocalDate] =
    EitherT.fromEither[Future](getUserDateOfBirthResult)

  def getAllBusinessesTaxableProfitAndLoss(taxYear: TaxYear, nino: Nino, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[List[TaxableProfitAndLoss]] =
    EitherT.fromEither[Future](getAllBusinessesTaxableProfitAndLossResult)

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummary] =
    EitherT.fromEither[Future](getBusinessIncomeSourcesSummaryResult)

  def getTotalTurnover(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[BigDecimal] =
    EitherT.fromEither[Future](getTotalTurnoverResult)

  def clearSimplifiedExpensesData(ctx: JourneyContextWithNino)(implicit request: DataRequest[_], hc: HeaderCarrier): ApiResultT[UserAnswers] =
    EitherT.fromEither[Future](clearSimplifiedExpensesDataResult)
}
