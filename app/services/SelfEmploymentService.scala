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

package services

import cats.data.EitherT
import connectors.SelfEmploymentConnector
import controllers.journeys.clearDependentPages
import models.Mode
import models.common.Journey.{allCapitalAllowanceJourneyPages, allExpensesJourneyPages}
import models.common.JourneyStatus.NotStarted
import models.common._
import models.database.UserAnswers
import models.domain.{ApiResultT, BusinessData, BusinessIncomeSourcesSummary}
import models.errors.ServiceError.IncomeAnswersNotSubmittedError
import models.journeys.adjustments.NetBusinessProfitOrLossValues
import models.journeys.income.IncomeJourneyAnswers
import models.journeys.nics.TaxableProfitAndLoss
import models.requests.DataRequest
import pages.{OneQuestionPage, QuestionPage, TradeAccountingType, TradingNameKey}
import play.api.Logging
import play.api.data.{Form, FormBinding}
import play.api.libs.json._
import play.api.mvc.Result
import queries.Settable
import repositories.SessionRepositoryBase
import services.SelfEmploymentService.clearDataFromUserAnswers
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.{nowarn, tailrec}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait SelfEmploymentService {
  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Seq[BusinessData]]

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[BusinessData]

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus]

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers]

  def persistAnswerAndRedirect[A: Writes](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          request: DataRequest[_],
                                          value: A,
                                          taxYear: TaxYear,
                                          mode: Mode): Future[Result]

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext,
                                             userAnswers: UserAnswers,
                                             declareJourneyAnswers: Option[SubsetOfAnswers] = None)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(TradingName, AccountingType, BusinessId)]): Future[UserAnswers]

  def submitGatewayQuestionAndClearDependentAnswers[A](pageUpdated: OneQuestionPage[A],
                                                       businessId: BusinessId,
                                                       userAnswers: UserAnswers,
                                                       newAnswer: A)(implicit reads: Reads[A], writes: Writes[A]): Future[UserAnswers]

  def submitGatewayQuestionAndRedirect[A](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          userAnswers: UserAnswers,
                                          newAnswer: A,
                                          taxYear: TaxYear,
                                          mode: Mode)(implicit reads: Reads[A], writes: Writes[A]): Future[Result]

  def handleForm[A](form: Form[A], handleError: Form[_] => Result, handleSuccess: A => Future[Result])(implicit
      request: DataRequest[_],
      defaultFormBinding: FormBinding): Future[Result]

  def defaultHandleForm[A](
      form: Form[A],
      page: OneQuestionPage[A],
      businessId: BusinessId,
      taxYear: TaxYear,
      mode: Mode,
      handleError: Form[_] => Result)(implicit request: DataRequest[_], defaultFormBinding: FormBinding, writes: Writes[A]): Future[Result]

  def getUserDateOfBirth(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[LocalDate]

  def getAllBusinessesTaxableProfitAndLoss(taxYear: TaxYear, nino: Nino, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[List[TaxableProfitAndLoss]]

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummary]

  def getNetBusinessProfitOrLossValues(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[NetBusinessProfitOrLossValues]

  def getTotalIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[BigDecimal]

  def clearSimplifiedExpensesData(ctx: JourneyContextWithNino)(implicit request: DataRequest[_], hc: HeaderCarrier): ApiResultT[Unit]

  def clearExpensesAndCapitalAllowances(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit]

  def clearOfficeSuppliesExpensesData(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit]

  def clearGoodsToSellOrUseExpensesData(taxYear: TaxYear, businessId: BusinessId)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit]

  def clearRepairsAndMaintenanceExpensesData(taxYear: TaxYear, businessId: BusinessId)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit]

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Boolean]
}

class SelfEmploymentServiceImpl @Inject() (
    connector: SelfEmploymentConnector,
    sessionRepository: SessionRepositoryBase,
    auditService: AuditService
)(implicit ec: ExecutionContext)
    extends SelfEmploymentService
    with Logging {

  def getBusinesses(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Seq[BusinessData]] =
    connector.getBusinesses(nino, mtditid)

  def getBusiness(nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[BusinessData] =
    connector.getBusiness(nino, businessId, mtditid)

  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus] =
    connector.getJourneyState(ctx.businessId, ctx.journey, ctx.taxYear, ctx.mtditid).map(_.journeyStatus)

  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    connector.saveJourneyState(ctx, status)

  /** Notice this method does two things:
    *   - setting (updating userAnswers under the passed page (make sure to use userAnswers returned by this method further in your program)
    *   - persisting in the database via sessionRepository
    */
  def persistAnswer[SubsetOfAnswers: Writes](businessId: BusinessId,
                                             userAnswers: UserAnswers,
                                             value: SubsetOfAnswers,
                                             page: QuestionPage[SubsetOfAnswers]): Future[UserAnswers] =
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set[SubsetOfAnswers](page, value, Some(businessId)))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext,
                                             userAnswers: UserAnswers,
                                             declareJourneyAnswers: Option[SubsetOfAnswers] = None)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val journeyAnswers: SubsetOfAnswers =
      declareJourneyAnswers.fold((userAnswers.data \ context.businessId.value).as[SubsetOfAnswers])(answers => answers)
    val journeyJson: JsObject = Json.toJson(journeyAnswers).as[JsObject]

    val result = connector.submitAnswers(context, journeyAnswers)
    sendAuditEvents(context, journeyJson, result)
  }

  private def sendAuditEvents(context: JourneyContext, answersJson: JsObject, resultT: ApiResultT[Unit])(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    resultT.value.onComplete {
      case Success(Right(_)) => auditService.unsafeSendExplicitCYAAuditEvent(context, answersJson, wasSuccessful = true)
      case _                 => auditService.unsafeSendExplicitCYAAuditEvent(context, answersJson, wasSuccessful = false)
    }

    resultT
  }

  @nowarn("msg=match may not be exhaustive")
  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(TradingName, AccountingType, BusinessId)]): Future[UserAnswers] =
    pairedIdsAndAccounting match {
      case Nil =>
        Future(userAnswers)
      case (tradingName: TradingName, accountingType: AccountingType, businessId: BusinessId) :: tail =>
        persistAnswer(businessId, userAnswers, accountingType, TradeAccountingType) flatMap { updatedUserAnswers: UserAnswers =>
          persistAnswer(businessId, updatedUserAnswers, tradingName, TradingNameKey) flatMap { updatedUserAnswers: UserAnswers =>
            setAccountingTypeForIds(updatedUserAnswers, tail)
          }
        }
    }

  def submitGatewayQuestionAndClearDependentAnswers[A](pageUpdated: OneQuestionPage[A],
                                                       businessId: BusinessId,
                                                       userAnswers: UserAnswers,
                                                       newAnswer: A)(implicit reads: Reads[A], writes: Writes[A]): Future[UserAnswers] =
    for {
      editedUserAnswers  <- clearDependentPages(pageUpdated, newAnswer, userAnswers, businessId)
      updatedUserAnswers <- persistAnswer(businessId, editedUserAnswers, newAnswer, pageUpdated)
    } yield updatedUserAnswers

  def submitGatewayQuestionAndRedirect[A](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          userAnswers: UserAnswers,
                                          newAnswer: A,
                                          taxYear: TaxYear,
                                          mode: Mode)(implicit reads: Reads[A], writes: Writes[A]): Future[Result] =
    submitGatewayQuestionAndClearDependentAnswers(pageUpdated, businessId, userAnswers, newAnswer)
      .map { updatedAnswers =>
        pageUpdated.redirectNext(mode, updatedAnswers, businessId, taxYear)
      }

  def persistAnswerAndRedirect[A: Writes](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          request: DataRequest[_],
                                          value: A,
                                          taxYear: TaxYear,
                                          mode: Mode): Future[Result] =
    persistAnswer(businessId, request.userAnswers, value, pageUpdated)
      .map { updatedAnswers =>
        pageUpdated.redirectNext(mode, updatedAnswers, businessId, taxYear)
      }

  def handleForm[A](form: Form[A], handleError: Form[_] => Result, handleSuccess: A => Future[Result])(implicit
      request: DataRequest[_],
      defaultFormBinding: FormBinding): Future[Result] =
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(handleError(formWithErrors)),
        answer => handleSuccess(answer)
      )

  def defaultHandleForm[A](
      form: Form[A],
      page: OneQuestionPage[A],
      businessId: BusinessId,
      taxYear: TaxYear,
      mode: Mode,
      handleError: Form[_] => Result)(implicit request: DataRequest[_], defaultFormBinding: FormBinding, writes: Writes[A]): Future[Result] = {
    def defaultHandleSuccess(answer: A)(implicit writes: Writes[A]): Future[Result] =
      persistAnswerAndRedirect(page, businessId, request, answer, taxYear, mode)

    handleForm(form, handleError, defaultHandleSuccess)
  }

  def getUserDateOfBirth(nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[LocalDate] =
    connector.getUserDateOfBirth(nino, mtditid)

  def getAllBusinessesTaxableProfitAndLoss(taxYear: TaxYear, nino: Nino, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[List[TaxableProfitAndLoss]] =
    connector.getAllBusinessIncomeSourcesSummaries(taxYear, nino, mtditid).map(_.map(TaxableProfitAndLoss.fromBusinessIncomeSourcesSummary))

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummary] =
    connector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId, mtditid)

  def getNetBusinessProfitOrLossValues(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      hc: HeaderCarrier): ApiResultT[NetBusinessProfitOrLossValues] =
    connector.getNetBusinessProfitOrLossValues(taxYear, nino, businessId, mtditid)

  def getTotalIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[BigDecimal] =
    connector.getSubmittedAnswers[IncomeJourneyAnswers](ctx).subflatMap {
      case Some(incomeAnswers) => Right(incomeAnswers.totalIncome)
      case None                => Left(IncomeAnswersNotSubmittedError)
    }

  def clearSimplifiedExpensesData(ctx: JourneyContextWithNino)(implicit request: DataRequest[_], hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _ <- setJourneyStatus(JourneyAnswersContext(ctx.taxYear, ctx.nino, ctx.businessId, ctx.mtditid, Journey.ExpensesTailoring), NotStarted)
      _ <- connector.clearExpensesSimplifiedOrNoExpensesAnswers(ctx.taxYear, ctx.nino, ctx.businessId, ctx.mtditid)
    } yield ()

  def clearExpensesAndCapitalAllowances(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val pagesToClear = allExpensesJourneyPages ++ allCapitalAllowanceJourneyPages
    val resultT = for {
      updateUserAnswers <- Future.fromTry(clearDataFromUserAnswers(request.userAnswers, pagesToClear, Some(businessId)))
      _                 <- sessionRepository.set(updateUserAnswers)
      result            <- connector.clearExpensesAndCapitalAllowances(taxYear, nino, businessId, mtditid).value
    } yield result
    EitherT(resultT)
  }

  def clearOfficeSuppliesExpensesData(taxYear: TaxYear, nino: Nino, businessId: BusinessId, mtditid: Mtditid)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit] =
    connector.clearOfficeSuppliesExpenses(taxYear, nino, businessId, mtditid)

  def clearGoodsToSellOrUseExpensesData(taxYear: TaxYear, businessId: BusinessId)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit] =
    connector.clearGoodsToSellOrUseExpensesData(taxYear, request.nino, businessId, request.mtditid)

  def clearRepairsAndMaintenanceExpensesData(taxYear: TaxYear, businessId: BusinessId)(implicit
      request: DataRequest[_],
      hc: HeaderCarrier): ApiResultT[Unit] =
    connector.clearRepairsAndMaintenanceExpensesData(taxYear, request.nino, businessId, request.mtditid)

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[Boolean] =
    connector.hasOtherIncomeSources(taxYear, nino, mtditid)
}

object SelfEmploymentService {

  def clearDataFromUserAnswers(userAnswers: UserAnswers, pages: List[Settable[_]], businessId: Option[BusinessId]): Try[UserAnswers] = {
    @tailrec
    def removePageData(userAnswers: UserAnswers, pages: List[Settable[_]]): Try[UserAnswers] =
      pages match {
        case Nil =>
          Try(userAnswers)
        case head :: tail =>
          userAnswers.remove(head, businessId) match {
            case Success(updatedUserAnswers) =>
              removePageData(updatedUserAnswers, tail)
            case Failure(exception) =>
              Failure(exception)
          }
      }

    removePageData(userAnswers, pages)
  }

}
