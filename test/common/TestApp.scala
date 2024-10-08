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

package common

import base.SpecBase
import base.SpecBase._
import controllers.actions.SubmittedDataRetrievalActionProvider
import models.common.{AccountingType, UserType}
import models.database.UserAnswers
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import services.SelfEmploymentService
import stubs.controllers.actions.StubSubmittedDataRetrievalActionProvider
import stubs.services.SelfEmploymentServiceStub

object TestApp {

  def buildApp(accountingType: AccountingType,
               userType: UserType,
               userAnswers: Option[UserAnswers] = None,
               serviceStub: Option[SelfEmploymentServiceStub] = None): Application = {
    val selfEmploymentService =
      serviceStub.getOrElse(SelfEmploymentServiceStub(accountingType = Right(accountingType), saveAnswerResult = emptyUserAnswers))
    SpecBase
      .applicationBuilder(userAnswers, userType)
      .overrides(
        bind[SelfEmploymentService].toInstance(selfEmploymentService),
        bind[SubmittedDataRetrievalActionProvider].toInstance(StubSubmittedDataRetrievalActionProvider())
      )
      .build()
  }

  def buildAppFromUserType(userType: UserType,
                           userAnswers: Option[UserAnswers] = None,
                           serviceStub: Option[SelfEmploymentServiceStub] = None): Application =
    buildApp(AccountingType.Cash, userType, userAnswers, serviceStub)

  def buildAppFromUserAnswers(userAnswers: UserAnswers): Application =
    buildApp(AccountingType.Cash, UserType.Individual, Some(userAnswers))

  def buildAppWithMessages() = {
    implicit val application  = SpecBase.applicationBuilder(None, UserType.Individual).build()
    val appMessages: Messages = messages(application)
    appMessages
  }

}
