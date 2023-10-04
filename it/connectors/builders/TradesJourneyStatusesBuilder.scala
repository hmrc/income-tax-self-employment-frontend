package connectors.builders

import models._
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyStatus
import play.api.libs.json.Json

object TradesJourneyStatusesBuilder {

  val aTadesJourneyStatusesModel = TradesJourneyStatuses("BusinessId1", Some("TradingName1"), Seq(
    JourneyStatus(Abroad, Some(true)),
    JourneyStatus(Income, Some(false)),
    JourneyStatus(Expenses, None),
    JourneyStatus(NationalInsurance, None)
  ))
  val aTadesJourneyStatusesRequestJson = Json.toJson(aTadesJourneyStatusesModel)

  val anEmptyTadesJourneyStatusesModel = TradesJourneyStatuses("BusinessId2", None, Seq.empty)
  val anEmptyTadesJourneyStatusesRequestJson = Json.toJson(anEmptyTadesJourneyStatusesModel)

  val aSequenceTadesJourneyStatusesModel = Seq(aTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel)
  val aSequenceTadesJourneyStatusesRequestString = Json.toJson(aSequenceTadesJourneyStatusesModel).toString()

}
