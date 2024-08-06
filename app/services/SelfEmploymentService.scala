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

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import controllers.journeys.clearDependentPages
import controllers.redirectJourneyRecovery
import models.Mode
import models.common._
import models.database.UserAnswers
import models.domain.ApiResultT
import models.requests.DataRequest
import pages.income.TurnoverIncomeAmountPage
import pages.{OneQuestionPage, QuestionPage, TradeAccountingType}
import play.api.Logging
import play.api.data.{Form, FormBinding}
import play.api.libs.json._
import play.api.mvc.Result
import queries.Settable
import repositories.SessionRepositoryBase
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.annotation.{nowarn, tailrec}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait SelfEmploymentService {
  def getJourneyStatus(ctx: JourneyAnswersContext)(implicit hc: HeaderCarrier): ApiResultT[JourneyStatus]
  def setJourneyStatus(ctx: JourneyAnswersContext, status: JourneyStatus)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def persistAnswer[A: Writes](businessId: BusinessId, userAnswers: UserAnswers, value: A, page: QuestionPage[A]): Future[UserAnswers]
  def persistAnswerAndRedirect[A: Writes](pageUpdated: OneQuestionPage[A],
                                          businessId: BusinessId,
                                          request: DataRequest[_],
                                          value: A,
                                          taxYear: TaxYear,
                                          mode: Mode): Future[Result]
  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(AccountingType, BusinessId)]): Future[UserAnswers]
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
}

class SelfEmploymentServiceImpl @Inject() (
    connector: SelfEmploymentConnector,
    sessionRepository: SessionRepositoryBase,
    auditService: AuditService
)(implicit ec: ExecutionContext)
    extends SelfEmploymentService
    with Logging {

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

  def submitAnswers[SubsetOfAnswers: Format](context: JourneyContext, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val journeyAnswers: SubsetOfAnswers = (userAnswers.data \ context.businessId.value).as[SubsetOfAnswers]
    val journeyJson: JsObject           = Json.toJson(journeyAnswers).as[JsObject]

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
  def setAccountingTypeForIds(userAnswers: UserAnswers, pairedIdsAndAccounting: Seq[(AccountingType, BusinessId)]): Future[UserAnswers] =
    pairedIdsAndAccounting match {
      case Nil =>
        Future(userAnswers)
      case (accountingType: AccountingType, businessId: BusinessId) :: tail =>
        persistAnswer(businessId, userAnswers, accountingType, TradeAccountingType) flatMap { updatedUserAnswers: UserAnswers =>
          setAccountingTypeForIds(updatedUserAnswers, tail)
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
}

object SelfEmploymentService {

  private val maxAllowance = BigDecimal(1000.00)

  // TODO: Return an error as left once we have impl. error handling instead of redirecting.
  def getMaxTradingAllowance(businessId: BusinessId, userAnswers: UserAnswers): Either[Result, BigDecimal] =
    userAnswers
      .get(TurnoverIncomeAmountPage, Some(businessId))
      .fold(redirectJourneyRecovery().asLeft[BigDecimal]) { turnover =>
        if (turnover > maxAllowance) maxAllowance.asRight else turnover.asRight
      }

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
