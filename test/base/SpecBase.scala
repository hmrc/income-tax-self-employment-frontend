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

import controllers.actions._
import models.common.AccountingType.{Accrual, Cash}
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import org.joda.time.LocalDate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait SpecBase extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  protected val individual: String = Individual.toString
  protected val agent: String      = Agent.toString

  protected val accrual: String = Accrual.toString
  protected val cash: String    = Cash.toString

  protected val taxYear: Int = LocalDate.now().getYear
  protected val businessId   = "SJPR05893938418"

  protected val enLang: Lang = Lang("en-EN")
  protected val cyLang: Lang = Lang("cy-CY")

  protected val userAnswersId                 = "id"
  protected val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application, isWelsh: Boolean = false): Messages =
    if (isWelsh) {
      app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("cy")))
    } else {
      app.injector.instanceOf[MessagesApi].preferred(FakeRequest().withHeaders())
    }

  protected def getLanguage(isWelsh: Boolean): String = if (isWelsh) "Welsh" else "English"

  protected def userType(isAgent: Boolean): String = if (isAgent) agent else individual

  protected def isAgent(authUserType: String): Boolean = authUserType.equals(agent)

  protected def isAccrual(accountingType: String) = accountingType.equals(accrual)

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

}
