/*
 * Copyright 2025 HM Revenue & Customs
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

import base.{ControllerTestScenarioSpec, SpecBase}
import builders.BusinessIncomeSourcesSummaryBuilder.aBusinessIncomeSourcesSummary
import builders.UserBuilder.aUserDateOfBirth
import cats.data.EitherT
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import connectors.SelfEmploymentConnector
import controllers.actions.SubmittedDataRetrievalActionProvider
import controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle.routes
import forms.standard.BooleanFormProvider
import models.NormalMode
import models.common.Journey.{ExpensesGoodsToSellOrUse, Income}
import models.common.UserType.Individual
import models.common._
import models.database.UserAnswers
import models.domain.BusinessIncomeSourcesSummary
import models.errors.ServiceError
import models.errors.ServiceError.NotFoundError
import models.journeys.JourneyNameAndStatus
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.{ZegvHowMuchDoYouWantToClaim, ZegvUseOutsideSE}
import models.journeys.income.{IncomeJourneyAnswers, IncomeJourneyAnswersTestData}
import models.journeys.nics.TaxableProfitAndLoss
import models.requests.DataRequest
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import play.api.data.{Form, FormBinding}
import play.api.http.Status.{BAD_REQUEST, SEE_OTHER}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.{BadRequest, Redirect}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.Settable.SetAnswer
import services.SelfEmploymentService.clearDataFromUserAnswers
import stubs.repositories.StubSessionRepository
import stubs.services.AuditServiceStub

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentServiceSpec extends SpecBase with ControllerTestScenarioSpec {

  implicit val messages: Messages = messagesStubbed

  val nino: Nino                    = Nino("nino")
  val businessIdAccrual: BusinessId = BusinessId("businessIdAccrual")
  val businessIdCash: BusinessId    = BusinessId("businessIdCash")

  "getJourneyStatus" - {
    "should return status" in new ServiceWithStubs {
      val status: JourneyNameAndStatus = JourneyNameAndStatus(ExpensesGoodsToSellOrUse, JourneyStatus.Completed)
      mockConnector.getJourneyState(any[BusinessId], any[Journey], any[TaxYear], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](status)

      val result: Either[ServiceError, JourneyStatus] =
        service.getJourneyStatus(JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesGoodsToSellOrUse)).value.futureValue

      result shouldBe status.journeyStatus.asRight
    }
  }

  "setJourneyStatus" - {
    "should save status" in new ServiceWithStubs {
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())
      val result: Either[ServiceError, Unit] = service
        .setJourneyStatus(JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesGoodsToSellOrUse), JourneyStatus.Completed)
        .value
        .futureValue
      result shouldBe ().asRight
    }
  }

  "submitAnswers" - {
    val userAnswerData = Json
      .parse(s"""
           |{
           |  "$businessId": {
           |  }
           |}
           |""".stripMargin)
      .as[JsObject]
    val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)
    val ctx                      = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesGoodsToSellOrUse)

    "submit answers to the connector" in new ServiceWithStubs {
      mockConnector.submitAnswers(any, any)(*, *, *) returns EitherT(Future.successful(().asRight[ServiceError]))

      val result: Either[ServiceError, Unit] = service.submitAnswers[JsObject](ctx, userAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "clearDataFromUserAnswers" - {
    val userAnswers = emptyUserAnswers
      .set(LivingAtBusinessPremisesOnePerson, 2, None)
      .success
      .value
      .set(LivingAtBusinessPremisesTwoPeople, 1, None)
      .success
      .value
      .set(LivingAtBusinessPremisesThreePlusPeople, 5, None)
      .success
      .value
    val fullPageList = List(LivingAtBusinessPremisesOnePerson, LivingAtBusinessPremisesTwoPeople, LivingAtBusinessPremisesThreePlusPeople)
    val emptyList    = List.empty
    "should clear UserAnswers data from pages in the list and return the result" in {
      val result = clearDataFromUserAnswers(userAnswers, fullPageList, None)

      result.success.value.data shouldBe Json.obj()
    }
    "should handle an empty list of pages" in {
      val result = clearDataFromUserAnswers(userAnswers, emptyList, None)

      result.success.value.data shouldBe Json.obj(
        "livingAtBusinessPremisesOnePerson"       -> 2,
        "livingAtBusinessPremisesTwoPeople"       -> 1,
        "livingAtBusinessPremisesThreePlusPeople" -> 5)
    }
    "should handle trying to clear data when there is none saved" in {
      val result = clearDataFromUserAnswers(emptyUserAnswers, fullPageList, None)

      result.success.value.data shouldBe Json.obj()
    }
  }

  "setAccountingTypeForIds" - {
    "should set the AccountingType of each supplied BusinessId to the UserAnswers, returning the updated UserAnswers when" - {
      "supplied a valid sequence of TradingName, BusinessIds and AccountingTypes" in new ServiceWithStubs {
        val testList: Seq[(TradingName, WithName with AccountingType, BusinessId)] = Seq(
          (TradingName("Circus Performer1"), AccountingType.Accrual, BusinessId("testId1")),
          (TradingName("Circus Performer2"), AccountingType.Cash, BusinessId("testId2")),
          (TradingName("Circus Performer3"), AccountingType.Accrual, BusinessId("testId3"))
        )
        val result: JsObject = await(service.setAccountingTypeForIds(emptyUserAnswers, testList)).data
        val expectedResult: JsObject = Json.obj(
          "testId1" -> Json.obj("accountingType" -> "ACCRUAL", "tradingName" -> "Circus Performer1"),
          "testId2" -> Json.obj("accountingType" -> "CASH", "tradingName" -> "Circus Performer2"),
          "testId3" -> Json.obj("accountingType" -> "ACCRUAL", "tradingName" -> "Circus Performer3")
        )

        result shouldBe expectedResult
      }
      "input sequence is empty" in new ServiceWithStubs {
        val result: JsObject = await(service.setAccountingTypeForIds(emptyUserAnswers, Seq.empty)).data

        result shouldBe Json.obj()
      }
    }
  }

  private val existingZegvAnswers = SetAnswer
    .setMany(businessId, emptyUserAnswers)(
      SetAnswer(ZegvAllowancePage, true),
      SetAnswer(ZegvClaimAmountPage, BigDecimal(200)),
      SetAnswer(ZegvHowMuchDoYouWantToClaimPage, ZegvHowMuchDoYouWantToClaim.LowerAmount),
      SetAnswer(ZegvOnlyForSelfEmploymentPage, true),
      SetAnswer(ZegvTotalCostOfVehiclePage, BigDecimal(100)),
      SetAnswer(ZegvUseOutsideSEPage, ZegvUseOutsideSE.Ten),
      SetAnswer(ZegvUseOutsideSEPercentagePage, 300)
    )
    .success
    .value
  private val expectedClearedAnswers = emptyUserAnswers.set(ZeroEmissionGoodsVehiclePage, false, Some(businessId)).success.value.data
  "submitGatewayQuestionAndClearDependentAnswers" - {
    "return UserAnswers with cleared dependent pages when selected No" in new ServiceWithStubs {
      val updatedAnswers: UserAnswers =
        service
          .submitGatewayQuestionAndClearDependentAnswers(ZeroEmissionGoodsVehiclePage, businessId, existingZegvAnswers, newAnswer = false)
          .futureValue

      val dbAnswers: JsObject = repository.state(userAnswersId).data
      assert(dbAnswers === expectedClearedAnswers)

      assert(updatedAnswers.data === expectedClearedAnswers)
    }
  }

  "submitGatewayQuestionAndRedirect" - {
    "return a Redirect to the next page and cleared dependent pages when answer is 'No'" in new ServiceWithStubs {
      val result: Future[Result] = service.submitGatewayQuestionAndRedirect(
        ZeroEmissionGoodsVehiclePage,
        businessId,
        existingZegvAnswers,
        newAnswer = false,
        taxYear,
        NormalMode)

      val dbAnswers: JsObject = repository.state(userAnswersId).data
      assert(dbAnswers === expectedClearedAnswers)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe ZeroEmissionGoodsVehiclePage.cyaPage(taxYear, businessId).url.some
    }
  }

  "persistAnswerAndRedirect" - {
    "save answer to session repository and return a Redirect to the next page" in new ServiceWithStubs {
      val result: Future[Result] =
        service
          .persistAnswerAndRedirect(
            ZeroEmissionGoodsVehiclePage,
            businessId,
            fakeDataRequest(existingZegvAnswers),
            value = false,
            taxYear,
            NormalMode)

      val expectedAnswers: UserAnswers = existingZegvAnswers.set(ZeroEmissionGoodsVehiclePage, false, businessId.some).success.value
      val dbAnswers: UserAnswers       = repository.state(userAnswersId)
      assert(dbAnswers === expectedAnswers)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe ZeroEmissionGoodsVehiclePage.cyaPage(taxYear, businessId).url.some
    }
  }

  "handleForm" - {
    "should attempt to bind from request" - {
      val page                = ZeroEmissionGoodsVehiclePage
      val form: Form[Boolean] = new BooleanFormProvider()(page, Individual)
      val request             = FakeRequest(POST, routes.ZeroEmissionGoodsVehicleController.onSubmit(taxYear, businessId, NormalMode).url)

      def handleError(formWithErrors: Form[_]): Result = BadRequest(formWithErrors.toString)

      def handleSuccess(answer: Boolean): Future[Result] = answer match {
        case _ => Future(Redirect(page.cyaPage(taxYear, businessId)))
      }

      "following the handleSuccess method if it binds successfully" in new ServiceWithStubs {
        val dataRequest: DataRequest[AnyContent] =
          DataRequest[AnyContent](request.withFormUrlEncodedBody(("value", true.toString)), "userId", fakeUser, existingZegvAnswers)
        val result: Future[Result] = service.handleForm(form, handleError, handleSuccess)(dataRequest, FormBinding.Implicits.formBinding)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe ZeroEmissionGoodsVehiclePage.cyaPage(taxYear, businessId).url.some
      }
      "following the handleError method if it binds unsuccessfully" in new ServiceWithStubs {
        val dataRequest: DataRequest[AnyContent] =
          DataRequest[AnyContent](request.withFormUrlEncodedBody(("value", "invalid value")), "userId", fakeUser, existingZegvAnswers)
        val result: Future[Result] = service.handleForm(form, handleError, handleSuccess)(dataRequest, FormBinding.Implicits.formBinding)

        status(result) shouldBe BAD_REQUEST
      }
    }
  }

  "getUserDateOfBirth" - {
    "should return a user's date of birth" in new ServiceWithStubs {
      mockConnector.getUserDateOfBirth(any[Nino], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](aUserDateOfBirth)

      val result: Either[ServiceError, LocalDate] = service.getUserDateOfBirth(nino, mtditid).value.futureValue

      result shouldBe aUserDateOfBirth.asRight
    }
  }

  "getAllBusinessesTaxableProfitAndLoss" - {
    "should return a list of any businesses' taxable profits and losses" in new ServiceWithStubs {
      mockConnector.getAllBusinessIncomeSourcesSummaries(any[TaxYear], any[Nino], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](List.empty[BusinessIncomeSourcesSummary])

      val result: Either[ServiceError, List[TaxableProfitAndLoss]] =
        service.getAllBusinessesTaxableProfitAndLoss(taxYear, nino, mtditid).value.futureValue

      result shouldBe List.empty[TaxableProfitAndLoss].asRight
    }
  }

  "getBusinessIncomeSourcesSummary" - {
    "should return a business income sources summary for a given business id" in new ServiceWithStubs {
      mockConnector.getBusinessIncomeSourcesSummary(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](aBusinessIncomeSourcesSummary)

      val result: Either[ServiceError, BusinessIncomeSourcesSummary] =
        service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId, mtditid).value.futureValue

      result shouldBe aBusinessIncomeSourcesSummary.asRight
    }
  }

  "getTotalIncome" - {
    val ctx = JourneyContextWithNino(taxYear, nino, businessId, mtditid, Income)

    "fail with Income Not Found error when no income" in new ServiceWithStubs {
      mockConnector.getSubmittedAnswers[IncomeJourneyAnswers](any[JourneyContext])(*, *, *) returns EitherT.rightT[Future, ServiceError](None)
      val result: Either[ServiceError, BigDecimal] = service.getTotalIncome(ctx).value.futureValue
      result shouldBe ServiceError.IncomeAnswersNotSubmittedError.asLeft
    }

    "calculate total turnover from income answers" in new ServiceWithStubs {
      val answers: IncomeJourneyAnswers = IncomeJourneyAnswersTestData.sample.copy(
        turnoverIncomeAmount = 5.0,
        nonTurnoverIncomeAmount = Some(10.0)
      )
      mockConnector.getSubmittedAnswers[IncomeJourneyAnswers](any[JourneyContext])(*, *, *) returns EitherT.rightT[Future, ServiceError](
        Some(answers))

      val result: Either[ServiceError, BigDecimal] = service.getTotalIncome(ctx).value.futureValue

      result shouldBe Right(15.0)
    }
  }

  "clearSimplifiedExpensesData" - {
    val ctx                                       = JourneyContextWithNino(taxYear, nino, businessId, mtditid, Income)
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete Simplified or No Expenses data from the front and back-end repos and API" in new ServiceWithStubs {
      mockConnector.clearExpensesSimplifiedOrNoExpensesAnswers(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearSimplifiedExpensesData(ctx).value.futureValue
      assert(result === ().asRight)
    }
  }

  "clearExpensesAndCapitalAllowances" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(emptyUserAnswers)

    "should delete Expenses and Capital Allowances data from the front and back-end repos and API" in new ServiceWithStubs {
      mockConnector.clearExpensesAndCapitalAllowances(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearExpensesAndCapitalAllowances(taxYear, nino, businessId, mtditid).value.futureValue
      assert(result === ().asRight)
    }

    "should delete Simplified or No Expenses data from the front and back-end repos and API" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearExpensesAndCapitalAllowances(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearExpensesAndCapitalAllowances(taxYear, nino, businessId, mtditid).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearOfficeSuppliesExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete office supplies expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearOfficeSuppliesExpenses(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearOfficeSuppliesExpensesData(taxYear, nino, businessId, mtditid).value.futureValue
      assert(result === ().asRight)
    }

    "should delete from the front and back-end repos and API" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearOfficeSuppliesExpenses(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearOfficeSuppliesExpensesData(taxYear, nino, businessId, mtditid).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearGoodsToSellOrUseExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete Goods to sell or use expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearGoodsToSellOrUseExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearGoodsToSellOrUseExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "should delete from the front and back-end repos and API" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearGoodsToSellOrUseExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearGoodsToSellOrUseExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearRepairsAndMaintenanceExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete repairs and maintenance expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearWorkplaceRunningCostsExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearWorkplaceRunningCostsExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "fail when delete from the front and back-end repos and API returns an error" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearWorkplaceRunningCostsExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearWorkplaceRunningCostsExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearWorkplaceRunningCostsExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete repairs and maintenance expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearWorkplaceRunningCostsExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearWorkplaceRunningCostsExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "fail when delete from the front and back-end repos and API returns an error" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearWorkplaceRunningCostsExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearWorkplaceRunningCostsExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearAdvertisingOrMarketingExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete advertising or marketing expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearAdvertisingOrMarketingExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearAdvertisingOrMarketingExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "fail when delete from the front and back-end repos and API returns an error" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearAdvertisingOrMarketingExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearAdvertisingOrMarketingExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearStaffCostsExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete repairs and maintenance expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearStaffCostsExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearStaffCostsExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "fail when delete from the front and back-end repos and API returns an error" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearStaffCostsExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearStaffCostsExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearConstructionExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete repairs and maintenance expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearConstructionExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearConstructionExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "fail when delete from the front and back-end repos and API returns an error" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearConstructionExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearConstructionExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "clearProfessionalFeesExpensesData" - {
    implicit val request: DataRequest[AnyContent] = fakeDataRequest(buildUserAnswers[BigDecimal](TotalExpensesPage, 3000))

    "delete professional fees expenses data from backend and API" in new ServiceWithStubs {
      mockConnector.clearProfessionalFeesExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](())
      mockConnector.saveJourneyState(any[JourneyAnswersContext], any[JourneyStatus])(*, *) returns EitherT.rightT[Future, ServiceError](())

      val result: Either[ServiceError, Unit] = service.clearProfessionalFeesExpensesData(taxYear, businessId).value.futureValue
      assert(result === ().asRight)
    }

    "fail when delete from the front and back-end repos and API returns an error" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.clearProfessionalFeesExpensesData(any[TaxYear], any[Nino], any[BusinessId], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Unit](downstreamError)

      val result: Either[ServiceError, Unit] = service.clearProfessionalFeesExpensesData(taxYear, businessId).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

  "hasOtherIncomeSources" - {

    "should return flag based on other income sources availability" in new ServiceWithStubs {
      mockConnector.hasOtherIncomeSources(any[TaxYear], any[Nino], any[Mtditid])(*, *) returns EitherT
        .rightT[Future, ServiceError](true)

      val result: Either[ServiceError, Boolean] = service.hasOtherIncomeSources(taxYear, nino, mtditid).value.futureValue

      result shouldBe true.asRight
    }

    "should fail with an error when down stream service fails" in new ServiceWithStubs {
      val downstreamError: NotFoundError = NotFoundError("NOT_FOUND")
      mockConnector.hasOtherIncomeSources(any[TaxYear], any[Nino], any[Mtditid])(*, *) returns EitherT
        .leftT[Future, Boolean](downstreamError)

      val result: Either[ServiceError, Boolean] = service.hasOtherIncomeSources(taxYear, nino, mtditid).value.futureValue
      assert(result === downstreamError.asLeft)
    }
  }

}

trait ServiceWithStubs {
  val mockConnector: SelfEmploymentConnector                                         = mock[SelfEmploymentConnector]
  val repository: StubSessionRepository                                              = StubSessionRepository()
  val mockSubmittedDataRetrievalActionProvider: SubmittedDataRetrievalActionProvider = mock[SubmittedDataRetrievalActionProvider]

  val service: SelfEmploymentService = new SelfEmploymentServiceImpl(mockConnector, repository, AuditServiceStub())
}
