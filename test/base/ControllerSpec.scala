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

import models.common._
import models.database.UserAnswers
import models.{Mode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future

trait ControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks {

  val userTypeCases = Table(
    "userType",
    UserType.Individual,
    UserType.Agent
  )

  val langUserTypeCases = Table(
    ("Language", "userType"),
    (Language.English, UserType.Individual),
    (Language.English, UserType.Agent),
    (Language.Welsh, UserType.Individual),
    (Language.Welsh, UserType.Agent)
  )

  val bindings: List[Binding[_]] = Nil

  case class TestScenario(userType: UserType,
                          answers: Option[UserAnswers],
                          mode: Mode = NormalMode,
                          taxYear: TaxYear = TaxYear(LocalDate.now().getYear),
                          businessId: BusinessId = BusinessId(stubbedBusinessId),
                          accountingType: Option[AccountingType] = None) {

    private val mockSessionRepository = mock[SessionRepository]

    implicit val application: Application = createApp(userType, answers, mockSessionRepository)
    implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]
    implicit val appMessages: Messages    = messages(application)

    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

    private def createApp(userType: UserType, answers: Option[UserAnswers], mockSessionRepository: SessionRepository): Application = {
      val overrideBindings: List[Binding[_]] = bind[SessionRepository].toInstance(mockSessionRepository) :: bindings

      applicationBuilder(answers, isAgent(userType.toString))
        .overrides(overrideBindings)
        .build()
    }

  }

}
