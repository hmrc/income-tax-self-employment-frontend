package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.SpecBase.{businessId, convertScalaFuture, emptyUserAnswers, fakeDataRequest, userAnswersId}
import models.NormalMode
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.{ZegvHowMuchDoYouWantToClaim, ZegvUseOutsideSE}
import org.scalatest.TryValues._
import org.scalatest.wordspec.AnyWordSpecLike
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import queries.Settable.SetAnswer
import services.SelfEmploymentServiceImpl
import stubs.connectors.SelfEmploymentConnectorStub
import stubs.repositories.StubSessionRepository

import scala.concurrent.ExecutionContext.Implicits.global

class ZeroEmissionGoodsVehicleServiceSpec extends AnyWordSpecLike {
  val existingAllAnswers = SetAnswer
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

  val fakeRequest                       = fakeDataRequest(existingAllAnswers)
  val repository: StubSessionRepository = StubSessionRepository()
  val serviceStub                       = new SelfEmploymentServiceImpl(SelfEmploymentConnectorStub(), repository)
  val underTest                         = new ZeroEmissionGoodsVehicleService(serviceStub)

  "submitAnswer" should {
    "return UserAnswers with cleared dependent pages when selected No" in {
      val (updatedAnswers, updatedMode) = underTest.submitAnswer(businessId, fakeRequest, newAnswer = false, NormalMode).futureValue

      val expectedAnswers = emptyUserAnswers
        .set(
          ZeroEmissionGoodsVehiclePage,
          false,
          Some(businessId)
        )
        .success
        .value
        .data

      assert(updatedAnswers.data === expectedAnswers)
      assert(updatedMode === NormalMode)
      val dbAnswers = repository.state(userAnswersId).data
      assert(dbAnswers === expectedAnswers)
    }
  }
}
