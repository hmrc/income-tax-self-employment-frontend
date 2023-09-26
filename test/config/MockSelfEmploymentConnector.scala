
package config

import connectors.SelfEmploymentConnector
import connectors.httpParsers.SelfEmploymentResponse.SelfEmploymentResponse
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
trait MockSelfEmploymentConnector extends MockFactory {
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]

  def mockSaveJourneyState(nino: String, journeyId: String, taxYear: Int, isComplete: Boolean, response: SelfEmploymentResponse):
    CallHandler5[String, String, Int, Boolean, HeaderCarrier, Future[SelfEmploymentResponse]] = {
      (mockConnector.saveJourneyState(_: String, _: String, _: Int, _: Boolean)(_: HeaderCarrier))
        .expects(nino, journeyId, taxYear, isComplete, *)
        .returns(Future.successful(response))
        .anyNumberOfTimes()
    }
}
