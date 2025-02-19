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
import data.TimeData
import models.common.UserType.Individual
import models.common._
import models.database.UserAnswers
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchersSugar
import org.mockito.Mockito.when
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import play.api.mvc.Result
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.contentAsString
import services.SelfEmploymentService
import stubs.services.SelfEmploymentServiceStub
import utils.TimeMachine

import java.time.LocalDate
import scala.concurrent.Future

trait ControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks with ArgumentMatchersSugar with ControllerTestScenarioSpec {

  val userTypeCases: TableFor1[UserType] = Table(
    "userType",
    UserType.Individual,
    UserType.Agent
  )
}

trait ControllerTestScenarioSpec extends MockitoSugar with DefaultAwaitTimeout with ArgumentMatchersSugar {

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]
  val mockTimeMachine: TimeMachine       = mock[TimeMachine]

  val bindings: List[Binding[_]] = Nil

  when(mockTimeMachine.now).thenReturn(TimeData.testDate)

  case class TestScenario(userType: UserType = Individual,
                          answers: Option[UserAnswers],
                          mode: Mode = NormalMode,
                          taxYear: TaxYear = TaxYear(TimeData.testDate.getYear),
                          businessId: BusinessId = SpecBase.businessId,
                          tradingName: TradingName = TradingName.empty,
                          accountingType: Option[AccountingType] = Some(AccountingType.Accrual),
                          service: SelfEmploymentService = mockService) {
    implicit val application: Application = createApp(userType, answers, service)
    implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
    implicit val appMessages: Messages    = SpecBase.messages(application)

    val testScenarioContext: Journey => JourneyContextWithNino = (journey: Journey) =>
      JourneyContextWithNino(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, Mtditid(UserBuilder.aNoddyUser.mtditid), journey)

    private def createApp(userType: UserType, answers: Option[UserAnswers], mockService: SelfEmploymentService): Application = {

      val overrideBindings: List[Binding[_]] =
        bind[SelfEmploymentService].toInstance(mockService) +: bindings :+ bind[TimeMachine].toInstance(mockTimeMachine)

      SpecBase
        .applicationBuilder(answers, userType)
        .overrides(overrideBindings)
        .build()
    }

  }

  case class TestStubbedScenario(userType: UserType = Individual,
                                 answers: Option[UserAnswers],
                                 mode: Mode = NormalMode,
                                 taxYear: TaxYear = TaxYear(TimeData.testDate.getYear),
                                 businessId: BusinessId = SpecBase.businessId,
                                 tradingName: TradingName = TradingName.empty,
                                 accountingType: Option[AccountingType] = Some(AccountingType.Accrual),
                                 stubbedService: SelfEmploymentService = SelfEmploymentServiceStub()) {
    implicit val application: Application = createApp(userType, answers, stubbedService)
    implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
    implicit val appMessages: Messages    = SpecBase.messages(application)

    val testScenarioContext: Journey => JourneyContextWithNino = (journey: Journey) =>
      JourneyContextWithNino(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, Mtditid(UserBuilder.aNoddyUser.mtditid), journey)

    private def createApp(userType: UserType, answers: Option[UserAnswers], stubbedService: SelfEmploymentService): Application = {
      val overrideBindings: List[Binding[_]] =
        bind[SelfEmploymentService].toInstance(stubbedService) +: bindings :+ bind[TimeMachine].toInstance(mockTimeMachine)

      SpecBase
        .applicationBuilder(answers, userType)
        .overrides(overrideBindings)
        .build()
    }

  }

  def getTitle(fut: Future[Result]): String = {
    val html = contentAsString(fut)
    val doc  = Jsoup.parse(html)
    doc.select("title").first().text()
  }
}
