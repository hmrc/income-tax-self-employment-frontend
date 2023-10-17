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

import builders.UserBuilder.aNoddyUser
import controllers.actions.AuthenticatedIdentifierAction.SessionValues
import controllers.actions._
import models.UserAnswers
import org.joda.time.LocalDate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

trait SpecBase extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience {

  val userAnswersId: String = "id"
  val taxYear: Int          = LocalDate.now().getYear
  val enLang                = Lang("en-EN")
  val cyLang                = Lang("cy-CY")

  implicit val ec                   = ExecutionContext.global
  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application, isWelsh: Boolean = false): Messages =
    if (isWelsh) {
      app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("cy")))
    } else {
      app.injector.instanceOf[MessagesApi].preferred(FakeRequest().withHeaders())
    }

  protected def isWelshToString(isWelsh: Boolean): String = if (isWelsh) "Welsh" else "English"

  protected def isAgentToString(isAgent: Boolean): String = if (isAgent) "agent" else "individual"

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None, isAgent: Boolean = false): GuiceApplicationBuilder = {
    val fakeIdentifierAction = {
      if (isAgent) {
        bind[IdentifierAction].to[FakeAgentIdentifierAction]
      } else {
        bind[IdentifierAction].to[FakeIndividualIdentifierAction]
      }
    }

    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        fakeIdentifierAction,
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
  }

  protected def buildRequest(httpType: String, requestRoute: String, isAgent: Boolean): FakeRequest[AnyContentAsEmpty.type] = {
    if (isAgent) {
      FakeRequest(httpType, requestRoute)
        .withSession(SessionValues.CLIENT_MTDITID -> aNoddyUser.mtditid, SessionValues.CLIENT_NINO -> aNoddyUser.nino)
    } else {
      FakeRequest(httpType, requestRoute)
    }
  }

}
