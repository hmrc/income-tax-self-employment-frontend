package services

import base.SpecBase
import connectors.SelfEmploymentConnector
import models.errors.APIErrorBody
import models.errors.APIErrorBody.APIStatusError
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import service.SelfEmploymentService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar {

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val selfEmploymentService = new SelfEmploymentService(mockConnector)

  val nino = "AA112233A"
  val journey = "journeyId"
  val taxYear = LocalDate.now().getYear
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "saveJourneyState" - {

    "must return a Right(()) when the connector returns a successful SelfEmploymentResponse" in {

      when(mockConnector.saveJourneyState(nino, journey, taxYear, isComplete = true)
      ) thenReturn Future(Right(()))

      val result = await(selfEmploymentService.saveJourneyState(nino, journey, taxYear, isComplete = true))
      result mustBe Right(())
    }

    "must return a Left(APIErrorModel) when the connector returns an error SelfEmploymentResponse" in {
      val invalidNinoResponse = APIStatusError(BAD_REQUEST, APIErrorBody.APIError("400", "Error"))
      when(mockConnector.saveJourneyState("fakeNino", journey, taxYear, isComplete = true)
      ) thenReturn Future(Left(invalidNinoResponse))

      val result = await(selfEmploymentService.saveJourneyState("fakeNino", journey, taxYear, isComplete = true))
      result mustBe Left(invalidNinoResponse)
    }

  }

}
