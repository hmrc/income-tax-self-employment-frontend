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
import org.mockito.ArgumentMatchersSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import repositories.SessionRepository
import services.SelfEmploymentService

import java.time.LocalDate

trait ControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks with ArgumentMatchersSugar {

  val userTypeCases = Table(
    "userType",
    UserType.Individual,
    UserType.Agent
  )

  val mockService = mock[SelfEmploymentService]

  val bindings: List[Binding[_]] = Nil

  case class TestScenario(userType: UserType,
                          answers: Option[UserAnswers],
                          mode: Mode = NormalMode,
                          taxYear: TaxYear = TaxYear(LocalDate.now().getYear),
                          businessId: BusinessId = businessId,
                          accountingType: Option[AccountingType] = None) {

    private val mockSessionRepository = mock[SessionRepository] // TODO Remove this? Controllers shouldn't interact with repository directly.

    implicit val application: Application = createApp(userType, answers, mockSessionRepository, mockService)
    implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
    implicit val appMessages: Messages    = messages(application)

    val testScenarioContext: Journey => JourneyContextWithNino = (journey: Journey) =>
      JourneyContextWithNino(taxYear, Nino(UserBuilder.aNoddyUser.nino), businessId, Mtditid(UserBuilder.aNoddyUser.mtditid), journey)

    private def createApp(userType: UserType,
                          answers: Option[UserAnswers],
                          mockSessionRepository: SessionRepository,
                          mockService: SelfEmploymentService): Application = {
      val overrideBindings: List[Binding[_]] =
        List(bind[SessionRepository].toInstance(mockSessionRepository), bind[SelfEmploymentService].toInstance(mockService)) ++ bindings

      applicationBuilder(answers, userType)
        .overrides(overrideBindings)
        .build()
    }

  }

}
