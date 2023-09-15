package services

import base.SpecBase
import connectors.SelfEmploymentConnector
import models.{APIErrorBodyModel, APIErrorModel}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.BAD_REQUEST
import play.api.test.Helpers._
import service.SelfEmploymentService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentServiceSpec extends SpecBase with MockitoSugar {

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]
  val selfEmploymentService = new SelfEmploymentService(mockConnector)

  val nino = "AA112233A"
  val journey = "journeyId"
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  "saveJourneyState" - {

    "must return a Right(()) when the connector returns a successful SelfEmploymentResponse" in {

      when(mockConnector.saveJourneyState(nino = nino, journeyId = journey, isComplete = true)
      ) thenReturn Future(Right(()))

      val result = await(selfEmploymentService.saveJourneyState(nino, journey, isComplete = true))
      result mustBe Right(())
    }

    "must return a Left(APIErrorModel) when the connector returns an error SelfEmploymentResponse" in {
      val invalidNinoResponse = APIErrorModel(BAD_REQUEST, APIErrorBodyModel("INVALID_NINO", "Submission has not passed validation. Invalid parameter"))
      when(mockConnector.saveJourneyState(nino = "fakeNino", journeyId = journey, isComplete = true)
      ) thenReturn Future(Left(invalidNinoResponse))

      val result = await(selfEmploymentService.saveJourneyState("fakeNino", journey, isComplete = true))
      result mustBe Left(invalidNinoResponse)
    }

  }

}
