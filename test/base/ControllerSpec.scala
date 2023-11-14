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

import models.common.{Language, UserType}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.BaseScalaTemplate
import models.common.UserType
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.mvc.Call
import play.twirl.api.BaseScalaTemplate
import repositories.SessionRepository
import play.api.inject.bind

import scala.concurrent.Future
import scala.reflect.ClassTag

trait ControllerSpec extends SpecBase with MockitoSugar with TableDrivenPropertyChecks {

  val langUserTypeCases = Table(
    ("Language", "userType"),
    (Language.English, UserType.Individual),
    (Language.English, UserType.Agent),
    (Language.Welsh, UserType.Individual),
    (Language.Welsh, UserType.Agent)
  )

  abstract class TestApp(userType: UserType, answers: Option[UserAnswers]) {
    private val mockSessionRepository = mock[SessionRepository]

    implicit val application              = createApp(userType, answers, mockSessionRepository)
    implicit val messagesApi: MessagesApi = application.injector.instanceOf[MessagesApi]

    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

  }

  def onwardRoute = Call("GET", "/foo")

  private def createApp(userType: UserType, answers: Option[UserAnswers], mockSessionRepository: SessionRepository) = {
    applicationBuilder(answers, isAgent(userType.toString))
      .overrides(
        bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()
  }

}
