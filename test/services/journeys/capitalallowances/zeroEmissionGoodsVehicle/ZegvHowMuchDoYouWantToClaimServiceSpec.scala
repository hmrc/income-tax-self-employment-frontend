package services.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.SpecBase.{ToFutureOps, convertScalaFuture, emptyUserAnswers}
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProvider.ZegvHowMuchDoYouWantToClaimModel
import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaim
import org.mockito.ArgumentMatchersSugar
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.TryValues._
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import pages.capitalallowances.zeroEmissionGoodsVehicle.ZegvClaimAmountPage
import services.SelfEmploymentService

import scala.concurrent.ExecutionContext.Implicits.global

class ZegvHowMuchDoYouWantToClaimServiceSpec extends AnyWordSpecLike with MockitoSugar with ArgumentMatchersSugar {
  val mockService     = mock[SelfEmploymentService]
  val expectedAnswers = emptyUserAnswers.set(ZegvClaimAmountPage, BigDecimal(123)).success.value
  val underTest       = new ZegvHowMuchDoYouWantToClaimService(mockService)

  "submitAnswer" should {
    "return a UserAnswers with the updated value" in {
      mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns expectedAnswers.asFuture

      val updatedAnswers = underTest
        .submitAnswer(
          emptyUserAnswers,
          ZegvHowMuchDoYouWantToClaimModel(ZegvHowMuchDoYouWantToClaim.FullCost, None),
          BigDecimal(10.0),
          base.SpecBase.businessId
        )
        .futureValue

      assert(updatedAnswers === expectedAnswers)
    }
  }
}
