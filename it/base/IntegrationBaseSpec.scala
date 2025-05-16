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

package base

import com.github.tomakehurst.wiremock.http.HttpHeader
import helpers.SessionCookieHelper
import integrationData.TimeData
import models.Index
import models.common._
import models.database.UserAnswers
import models.domain.BusinessData
import models.domain.BusinessData.{AccountingPeriod, LatencyDetails}
import models.errors.ServiceError.ConnectorResponseError
import models.errors.{HttpError, HttpErrorBody, ServiceError}
import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import models.journeys.expenses.travelAndAccommodation.{
  LeasedVehicles,
  OwnVehicles,
  TravelExpensesDb,
  VehicleDetailsDb,
  YourFlatRateForVehicleExpenses
}
import org.mockito.Mockito.when
import org.mongodb.scala.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.DeleteResult
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.HeaderNames
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.TimeMachine

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures with BeforeAndAfterEach with SessionCookieHelper {

  val mockTimeMachine: TimeMachine = mock[TimeMachine]

  when(mockTimeMachine.now).thenReturn(TimeData.testDate)

  protected val internalId: String     = "1"
  protected val businessId: BusinessId = BusinessId("someBusinessId")
  protected val nino: Nino             = Nino("AA123123A")
  protected val mtditid: Mtditid       = IntegrationBaseSpec.mtditid
  protected val taxYear: TaxYear       = TaxYear(mockTimeMachine.now.getYear)
  protected val index: Index           = Index(1)

  val testVehicleDetails: VehicleDetailsDb = VehicleDetailsDb(
    description = Some("Car"),
    vehicleType = Some(CarOrGoodsVehicle),
    usedSimplifiedExpenses = Some(true),
    calculateFlatRate = Some(true),
    workMileage = Some(100000),
    expenseMethod = Some(YourFlatRateForVehicleExpenses.Flatrate),
    costsOutsideFlatRate = Some(BigDecimal("100.00")),
    vehicleExpenses = Some(BigDecimal("300.00"))
  )

  val testTravelAndAccommodationData: TravelExpensesDb = TravelExpensesDb(
    expensesToClaim = Some(Seq(OwnVehicles, LeasedVehicles)),
    allowablePublicTransportExpenses = Some(1000.00),
    disallowablePublicTransportExpenses = Some(100.00),
    totalTravelExpenses = Some(500.00),
    disallowableTravelExpenses = Some(400.00)
  )

  val businessData: BusinessData =
    BusinessData(
      businessId = "businessId-1",
      typeOfBusiness = "self-employment",
      tradingName = Some("Trade one"),
      yearOfMigration = Some("2022"),
      accountingPeriods = Seq(AccountingPeriod("2023-0x2-29", "2024-0x2-29")),
      firstAccountingPeriodStartDate = Some("2019-09-30"),
      firstAccountingPeriodEndDate = Some("2020-02-29"),
      latencyDetails = Some(LatencyDetails("2020-02-27", "2019", "A", "2020", "A")),
      accountingType = Some("ACCRUAL"),
      commencementDate = Some("2023-04-06"),
      cessationDate = Some("2024-04-05"),
      businessAddressLineOne = "Business Address",
      businessAddressLineTwo = Some("Business Address 2"),
      businessAddressLineThree = Some("Business Address 3"),
      businessAddressLineFour = Some("Business Address 4"),
      businessAddressPostcode = Some("Business Address 5"),
      businessAddressCountryCode = "GB"
    )

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = Span(sys.env.get("INTEGRATION_TEST_PATIENCE_TIMEOUT_SEC").fold(2)(x => Integer.parseInt(x)), Seconds),
    interval = Span(500, Millis)
  )

  protected val headersSentToBE: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", mtditid.value))

  protected def parsingError(method: String, url: String): ServiceError =
    ConnectorResponseError(method, url, HttpError(BAD_REQUEST, HttpErrorBody.parsingError))

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId(sessionId)))

  protected lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  protected def buildClient(urlandUri: String,
                            isAgent: Boolean = false,
                            extraHeaders: Option[Map[String, String]] = None,
                            extraSessionValues: Option[Map[String, String]] = None): WSRequest =
    ws
      .url(s"http://localhost:$port$urlandUri")
      .withFollowRedirects(false)
      .addHttpHeaders("Csrf-Token" -> "nocheck")
      .addHttpHeaders(extraHeaders.getOrElse(Map.empty).toSeq: _*)
      .addHttpHeaders(HeaderNames.COOKIE -> bakeSessionCookie(isAgent, extraSessionValues))

  override def afterEach(): Unit = {
    super.beforeEach()
    DbHelper.teardown
  }

  object DbHelper {

    val testAnswers: UserAnswers = UserAnswers(
      internalId,
      data = Json.obj()
    )

    val mongo: SessionRepository = app.injector.instanceOf[SessionRepository]

    val filter: Bson = Filters.equal("id", internalId)

    def insertMany(journeys: (Journey, JsValue)*): Unit = {
      val answers     = journeys.map { case (section, json) => section.entryName -> json }.toMap
      val userAnswers = testAnswers.copy(data = Json.toJson(answers).as[JsObject])
      await(mongo.collection.insertOne(userAnswers).toFuture())
    }

    def insertUserAnswers(answers: UserAnswers): Unit =
      await(mongo.collection.insertOne(answers).toFuture())

    def insertEmpty(): Unit =
      await(mongo.collection.insertOne(testAnswers).toFuture())

    def insertJson(journey: Journey, data: JsValue): Unit =
      insertMany(journey -> data)

    def insertOne[T](journey: Journey, data: T)(implicit format: Format[T]): Unit =
      insertMany(journey -> Json.toJson(data))

    def getJson(journey: Journey): Option[JsValue] = {
      val optAnswers = await(mongo.collection.find[UserAnswers](filter).headOption())
      optAnswers.flatMap(answers => (answers.data \ journey.entryName).asOpt[JsValue])
    }

    def get[T](journey: Journey)(implicit format: Format[T]): Option[T] = {
      val optAnswers = await(mongo.collection.find(filter).headOption())
      optAnswers.flatMap(answers => (answers.data \ journey.entryName).validate[T].asOpt)
    }

    def teardown: DeleteResult =
      await(mongo.collection.deleteMany(Document()).toFuture())
  }
}

object IntegrationBaseSpec {
  val mtditid: Mtditid = Mtditid("555555555")
}
