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
import models.common.Language._
import models.common.UserType.{Agent, Individual}
import models.common._
import models.database.UserAnswers
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.I18nSupport.ResultWithMessagesApi
import play.api.i18n._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait SpecBase extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience {

  val taxYear: TaxYear   = TaxYear(LocalDate.now().getYear)
  val userAnswersId      = "id"
  val individual: String = Individual.toString
  val agent: String      = Agent.toString
  val accrual: String    = Accrual.entryName
  val cash: String       = Cash.entryName
  val stubbedBusinessId  = "SJPR05893938418"
  val someNino: Nino     = Nino("someNino")
  val mtditid            = "someId"
  val businessId: BusinessId = BusinessId(
    stubbedBusinessId
  ) // TODO Use richer types everywhere, not primitive, and then clean up different similar names here

  def anyNino: Nino             = Nino(any)
  def anyMtditid: Mtditid       = Mtditid(any)
  def anyTaxYear: TaxYear       = TaxYear(any)
  def anyBusinessId: BusinessId = BusinessId(any)

  val enLang: Lang = Lang("en-EN")
  val cyLang: Lang = Lang("cy-CY")

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val hc: HeaderCarrier            = HeaderCarrier()

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application, lang: Language): Messages = {
    val bool = lang match {
      case Welsh   => true
      case English => false
    }
    messages(app, bool)
  }

  def messages(app: Application, isWelsh: Boolean = false): Messages =
    if (isWelsh) {
      app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("cy")))
    } else {
      app.injector.instanceOf[MessagesApi].preferred(FakeRequest().withHeaders())
    }

  /** This does not load real values from messages.en */
  def messagesStubbed: Messages = {
    val messagesApi: DefaultMessagesApi = new DefaultMessagesApi()
    MessagesImpl(Lang("en"), messagesApi)
  }

  protected def getLanguage(isWelsh: Boolean): String = if (isWelsh) "Welsh" else "English"

  protected def userType(isAgent: Boolean): String = if (isAgent) agent else individual

  protected def isAgent(authUserType: String): Boolean = authUserType.equals(agent)

  protected def isAccrual(accountingType: String): Boolean = accountingType.equals(accrual)

  def applicationBuilder(userAnswers: Option[UserAnswers], userType: UserType): GuiceApplicationBuilder =
    applicationBuilder(userAnswers, isAgent(userType.toString))

  def applicationBuilder(userAnswers: Option[UserAnswers] = None, isAgent: Boolean = false): GuiceApplicationBuilder = {
    val fakeIdentifierAction =
      if (isAgent) {
        bind[IdentifierAction].to[FakeAgentIdentifierAction]
      } else {
        bind[IdentifierAction].to[FakeIndividualIdentifierAction]
      }

    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        fakeIdentifierAction,
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
  }

  def languageAwareResult(lang: Language, result: Result)(implicit messagesApi: MessagesApi): Result =
    lang match {
      case English => result
      case Welsh   => result.withLang(cyLang)
    }

}

object SpecBase extends SpecBase
