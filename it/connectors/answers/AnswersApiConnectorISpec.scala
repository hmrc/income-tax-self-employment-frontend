package connectors.answers

import base.IntegrationBaseSpec
import helpers.{AnswersApiStub, WiremockSpec}
import models.Index
import models.common.Journey.ExpensesTravelForWork
import models.common.JourneyAnswersContext
import models.journeys.expenses.travelAndAccommodation.{OwnVehicles, TravelExpensesDb}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException

class AnswersApiConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  val connector: AnswersApiConnector     = app.injector.instanceOf[AnswersApiConnector]
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, ExpensesTravelForWork)

  val testTravelExpensesAnswers: TravelExpensesDb = TravelExpensesDb(
    expensesToClaim = Some(Seq(OwnVehicles)),
    allowablePublicTransportExpenses = Some(BigDecimal("100.00")),
    disallowablePublicTransportExpenses = Some(BigDecimal("50.00"))
  )

  "getAnswers" when {
    "the optional index parameter isn't provided" must {
      "return the correct model if the API returns OK" in {
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testTravelExpensesAnswers)))

        val result = await(connector.getAnswers[TravelExpensesDb](testContext))

        result mustBe Some(testTravelExpensesAnswers)
      }

      "return None if the API returns OK, but the returned JSON isn't valid" in {
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.obj("expensesToClaim" -> "Lorry")))

        val result = await(connector.getAnswers[TravelExpensesDb](testContext))

        result mustBe None
      }

      "return None if the API returns NOT FOUND" in {
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)

        val result = await(connector.getAnswers[TravelExpensesDb](testContext))

        result mustBe None
      }

      "throw an internal server exception for any other status" in {
        AnswersApiStub.getAnswers(testContext)(BAD_GATEWAY)

        assertThrows[InternalServerException] {
          await(connector.getAnswers[TravelExpensesDb](testContext))
        }
      }
    }

    "the optional index parameter is provided" must {
      "return the correct model if the API returns OK for the index" in {
        AnswersApiStub.getIndex(testContext, 1)(OK, Some(Json.toJson(testTravelExpensesAnswers)))
        AnswersApiStub.getIndex(testContext, 2)(OK, Some(Json.toJson(testTravelExpensesAnswers)))

        val result  = await(connector.getAnswers[TravelExpensesDb](testContext, index = Some(Index(1))))
        val result2 = await(connector.getAnswers[TravelExpensesDb](testContext, index = Some(Index(2))))

        result mustBe Some(testTravelExpensesAnswers)
        result2 mustBe Some(testTravelExpensesAnswers)
      }

      "return None if the API returns OK for the index, but the returned JSON isn't valid" in {
        AnswersApiStub.getIndex(testContext, 1)(OK, Some(Json.obj("expensesToClaim" -> "Lorry")))

        val result = await(connector.getAnswers[TravelExpensesDb](testContext, index = Some(Index(1))))

        result mustBe None
      }

      "return None if the API returns NOT FOUND for the index" in {
        AnswersApiStub.getIndex(testContext, 1)(NOT_FOUND)

        val result = await(connector.getAnswers[TravelExpensesDb](testContext, index = Some(Index(1))))

        result mustBe None
      }

      "throw an internal server exception for any other status" in {
        AnswersApiStub.getIndex(testContext, 1)(BAD_GATEWAY)

        assertThrows[InternalServerException] {
          await(connector.getAnswers[TravelExpensesDb](testContext, index = Some(Index(1))))
        }
      }
    }
  }

  "getAnswersAsList" must {
    "return the correct model if the API returns OK" in {
      val testTravelExpensesAnswers2 = testTravelExpensesAnswers.copy(allowablePublicTransportExpenses = Some(BigDecimal("200.00")))

      AnswersApiStub.getAnswers(testContext)(
        status = OK,
        response = Some(Json.obj("values" -> Json.toJson(Seq(testTravelExpensesAnswers, testTravelExpensesAnswers2))))
      )

      val result = await(connector.getAnswersAsList[TravelExpensesDb](testContext))

      result mustBe List(testTravelExpensesAnswers, testTravelExpensesAnswers2)
    }

    "return Nil if the API returns NOT FOUND" in {
      AnswersApiStub.getAnswers(testContext)(NOT_FOUND)

      val result = await(connector.getAnswersAsList[TravelExpensesDb](testContext))

      result mustBe Nil
    }

    "throw an internal server exception for any other status" in {
      AnswersApiStub.getAnswers(testContext)(BAD_GATEWAY)

      assertThrows[InternalServerException] {
        await(connector.getAnswersAsList[TravelExpensesDb](testContext))
      }
    }
  }

  "replaceAnswers" when {
    "the optional index parameter isn't provided" must {
      "return the created/updated answers when the API returns OK" in {
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testTravelExpensesAnswers))(OK)

        val result = await(connector.replaceAnswers[TravelExpensesDb](testContext, testTravelExpensesAnswers))

        result mustBe testTravelExpensesAnswers
      }

      "throw an internal server exception for any other status" in {
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testTravelExpensesAnswers))(BAD_GATEWAY)

        assertThrows[InternalServerException] {
          await(connector.replaceAnswers[TravelExpensesDb](testContext, testTravelExpensesAnswers))
        }
      }
    }

    "the optional index parameter is provided" must {
      "return the created/updated answers for the index when the API returns OK" in {
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testTravelExpensesAnswers))(OK)

        val result = await(connector.replaceAnswers[TravelExpensesDb](testContext, testTravelExpensesAnswers))

        result mustBe testTravelExpensesAnswers
      }

      "throw an internal server exception for any other status" in {
        AnswersApiStub.replaceIndex(testContext, Json.toJson(testTravelExpensesAnswers), index = 1)(BAD_GATEWAY)

        assertThrows[InternalServerException] {
          await(connector.replaceAnswers[TravelExpensesDb](testContext, testTravelExpensesAnswers, index = Some(Index(1))))
        }
      }
    }
  }

  "replaceAnswersAsList" must {
    "return the created/updated answers when the API returns OK" in {
      val testTravelExpensesAnswers2 = testTravelExpensesAnswers.copy(allowablePublicTransportExpenses = Some(BigDecimal("200.00")))

      AnswersApiStub.replaceAnswers(testContext, Json.obj("values" -> Json.toJson(List(testTravelExpensesAnswers, testTravelExpensesAnswers2))))(OK)

      val result = await(connector.replaceAnswersAsList[TravelExpensesDb](testContext, List(testTravelExpensesAnswers, testTravelExpensesAnswers2)))

      result mustBe Seq(testTravelExpensesAnswers, testTravelExpensesAnswers2)
    }

    "throw an internal server exception for any other status" in {
      AnswersApiStub.replaceAnswers(testContext, Json.obj("values" -> Json.toJson(List(testTravelExpensesAnswers))))(BAD_GATEWAY)

      assertThrows[InternalServerException] {
        await(connector.replaceAnswersAsList[TravelExpensesDb](testContext, List(testTravelExpensesAnswers)))
      }
    }
  }

  "deleteAnswers" when {
    "the optional index parameter isn't provided" must {
      "return true if the API returns NO CONTENT" in {
        AnswersApiStub.deleteAnswers(testContext)(NO_CONTENT)

        val result = await(connector.deleteAnswers(testContext))

        result mustBe true
      }

      "throw an internal server exception if any other response is received" in {
        AnswersApiStub.deleteAnswers(testContext)(BAD_GATEWAY)

        assertThrows[InternalServerException] {
          await(connector.deleteAnswers(testContext))
        }
      }
    }

    "the optional index parameter is provided" must {
      "return true if the operation succeeds" in {
        AnswersApiStub.deleteIndex(testContext, index = 1)(NO_CONTENT)

        val result = await(connector.deleteAnswers(testContext, index = Some(Index(1))))

        result mustBe true
      }

      "throw an internal server exception if any other response is received" in {
        AnswersApiStub.deleteIndex(testContext, index = 1)(BAD_GATEWAY)

        assertThrows[InternalServerException] {
          await(connector.deleteAnswers(testContext, index = Some(Index(1))))
        }
      }
    }
  }

}
