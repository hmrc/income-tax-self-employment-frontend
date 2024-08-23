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

package models.requests

import controllers.actions.AuthenticatedIdentifierAction.User
import controllers.standard
import models.common._
import models.database.UserAnswers
import models.common.Journey
import pages.{TradeAccountingType, TradingNameKey}
import play.api.libs.json.Reads
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result, WrappedRequest}
import queries.Gettable

import scala.concurrent.{ExecutionContext, Future}

sealed trait NinoDataRequest {
  val userType: UserType
  val nino: Nino
  val mtditid: Mtditid
}

case class OptionalDataRequest[A](request: Request[A], userId: String, user: User, userAnswers: Option[UserAnswers])
    extends WrappedRequest[A](request)
    with NinoDataRequest {
  val userType: UserType   = user.userType
  val nino: Nino           = Nino(user.nino)
  val answers: UserAnswers = userAnswers.getOrElse(UserAnswers(userId))
  val mtditid: Mtditid     = Mtditid(user.mtditid)

  def mkJourneyNinoContext(taxYear: TaxYear, businessId: BusinessId, journey: Journey, extraContext: Option[String] = None): JourneyContextWithNino =
    JourneyContextWithNino(taxYear, nino, businessId, mtditid, journey, extraContext)
}

case class DataRequest[A](request: Request[A], userId: String, user: User, userAnswers: UserAnswers)
    extends WrappedRequest[A](request)
    with NinoDataRequest {
  val userType: UserType = user.userType
  val nino: Nino         = Nino(user.nino)
  val mtditid: Mtditid   = Mtditid(user.mtditid)

  def getValue[B: Reads](page: Gettable[B], businessId: BusinessId): Option[B] =
    userAnswers.get(page, Some(businessId))

  def getAccountingType(businessId: BusinessId): Option[AccountingType] =
    userAnswers.get(TradeAccountingType, Some(businessId))

  def getTraderName(businessId: BusinessId): Option[TradingName] = userAnswers.get(TradingNameKey, Some(businessId))

  def valueOrRedirectDefault[B: Reads](page: Gettable[B], businessId: BusinessId): Either[Result, B] =
    getValue(page, businessId).toRight(Redirect(standard.routes.JourneyRecoveryController.onPageLoad()))

  def valueOrFutureRedirectDefault[B: Reads](page: Gettable[B], businessId: BusinessId)(implicit ec: ExecutionContext): Either[Future[Result], B] =
    getValue(page, businessId).toRight(Future(Redirect(standard.routes.JourneyRecoveryController.onPageLoad())))

}
