package connectors.builders

import models._
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyStatus
import play.api.libs.json.Json

object TradesJourneyStatusesBuilder {

  val aTaggedTradeDetailsModel = TradesJourneyStatuses("BusinessId1", Some("TradingName1"), Seq(
    JourneyStatus(Abroad, Some(true)),
    JourneyStatus(Income, Some(false)),
    JourneyStatus(Expenses, None),
    JourneyStatus(NationalInsurance, None)
  ))
  val aTaggedTradeDetailsRequestJson = Json.toJson(aTaggedTradeDetailsModel)

  val anEmptyTaggedTradeDetailsModel = TradesJourneyStatuses("BusinessId2", None, Seq.empty)
  val anEmptyTaggedTradeDetailsRequestJson = Json.toJson(anEmptyTaggedTradeDetailsModel)

  val aSequenceTaggedTradeDetailsModel = Seq(aTaggedTradeDetailsModel, anEmptyTaggedTradeDetailsModel)
  val aSequenceTaggedTradeDetailsRequestString = Json.toJson(aSequenceTaggedTradeDetailsModel).toString()

}
