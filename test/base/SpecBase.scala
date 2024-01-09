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

import builders.UserBuilder
import controllers.actions._
import models.common.UserType.Individual
import models.common._
import models.database.UserAnswers
import models.errors.HttpError
import models.errors.HttpErrorBody.SingleErrorBody
import models.journeys.Journey
import models.requests.OptionalDataRequest
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.http.Status.BAD_REQUEST
import play.api.i18n._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.SelfEmploymentServiceBase
import stubs.controllers.actions.{StubDataRetrievalAction, StubSubmittedDataRetrievalAction, StubSubmittedDataRetrievalActionProvider}
import stubs.services.SelfEmploymentServiceStub
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait SpecBase extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience {

  val taxYear: TaxYear           = TaxYear(LocalDate.now().getYear)
  val userAnswersId              = "id"
  val someNino: Nino             = Nino("someNino")
  val mtditid                    = "someId"
  val businessId: BusinessId     = BusinessId("SJPR05893938418")
  val tradingName: TradingName   = TradingName("Circus Performer")
  val zeroValue: BigDecimal      = 0
  val maxAmountValue: BigDecimal = 100000000000.00

  val fakeUser = AuthenticatedIdentifierAction.User(mtditid = "1234567890", arn = None, nino = "AA112233A", AffinityGroup.Individual.toString)
  val fakeOptionalRequest: OptionalDataRequest[AnyContent] = OptionalDataRequest[AnyContent](FakeRequest(), "userId", fakeUser, None)

  def anyNino: Nino               = Nino(any)
  def anyMtditid: Mtditid         = Mtditid(any)
  def anyTaxYear: TaxYear         = TaxYear(any)
  def anyBusinessId: BusinessId   = BusinessId(any)
  def anyUserAnswers: UserAnswers = UserAnswers(any)

  val submissionContext: Journey => JourneyContextWithNino =
    (journey: Journey) =>
      JourneyContextWithNino(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, Mtditid(UserBuilder.aNoddyUser.mtditid), journey)

  val enLang: Lang = Lang("en-EN")

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val hc: HeaderCarrier            = HeaderCarrier()

  val httpError: HttpError = HttpError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error parsing response from CONNECTOR"))

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def buildUserAnswers(data: JsObject): UserAnswers = UserAnswers(userAnswersId, Json.obj(businessId.value -> data))

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest().withHeaders())

  /** This does not load real values from messages.en */
  def messagesStubbed: Messages = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  def applicationBuilder(userAnswers: Option[UserAnswers] = None, userType: UserType = Individual): GuiceApplicationBuilder = {
    val fakeIdentifierAction =
      if (userType == Individual) {
        bind[IdentifierAction].to[FakeIndividualIdentifierAction]
      } else {
        bind[IdentifierAction].to[FakeAgentIdentifierAction]
      }

    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        fakeIdentifierAction,
        bind[DataRetrievalAction].toInstance(StubDataRetrievalAction(userAnswers)),
        bind[SubmittedDataRetrievalAction].toInstance(StubSubmittedDataRetrievalAction())
      )
  }

  def createApp(stub: SelfEmploymentServiceStub) =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[SelfEmploymentServiceBase].toInstance(stub))
      .build()

  def createApp(stub: StubSubmittedDataRetrievalActionProvider) =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[SubmittedDataRetrievalActionProvider].toInstance(stub))
      .build()

}

object SpecBase extends SpecBase
