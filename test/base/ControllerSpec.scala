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
import models.common._
import models.database.UserAnswers
import models.journeys.Journey
import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchersSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import play.api.mvc.Result
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.contentAsString
import services.SelfEmploymentService

import java.time.LocalDate
import scala.concurrent.Future

trait ControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks with ArgumentMatchersSugar with ControllerTestScenarioSpec {

  val userTypeCases = Table(
    "userType",
    UserType.Individual,
    UserType.Agent
  )
}

trait ControllerTestScenarioSpec extends MockitoSugar with DefaultAwaitTimeout with ArgumentMatchersSugar {

  val mockService = mock[SelfEmploymentService]

  val bindings: List[Binding[_]] = Nil

  case class TestScenario(userType: UserType,
                          answers: Option[UserAnswers],
                          mode: Mode = NormalMode,
                          taxYear: TaxYear = TaxYear(LocalDate.now().getYear),
                          businessId: BusinessId = SpecBase.businessId,
                          accountingType: Option[AccountingType] = None,
                          service: SelfEmploymentService = mockService) {
    implicit val application: Application = createApp(userType, answers, service)
    implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
    implicit val appMessages: Messages    = SpecBase.messages(application)

    val testScenarioContext: Journey => JourneyContextWithNino = (journey: Journey) =>
      JourneyContextWithNino(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, Mtditid(UserBuilder.aNoddyUser.mtditid), journey)

    private def createApp(userType: UserType, answers: Option[UserAnswers], mockService: SelfEmploymentService): Application = {
      val overrideBindings: List[Binding[_]] =
        bind[SelfEmploymentService].toInstance(mockService) :: bindings

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
