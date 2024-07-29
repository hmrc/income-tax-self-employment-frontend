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

package controllers.journeys.nics

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.BusinessId.nationalInsuranceContributions
import models.database.UserAnswers
import models.journeys.nics.ExemptionReason
import models.journeys.nics.ExemptionReason.TrusteeExecutorAdmin
import org.mockito.Mockito.when
import pages.nics.Class4ExemptionReasonPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import views.html.journeys.nics.Class4ExemptionReasonView

import scala.concurrent.Future

class Class4ExemptionReasonControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[ExemptionReason]("Class4ExemptionReasonController", Class4ExemptionReasonPage) {

  override def onPageLoadCall: Call         = routes.Class4ExemptionReasonController.onPageLoad(taxYear, NormalMode)
  override def onSubmitCall: Call           = routes.Class4ExemptionReasonController.onSubmit(taxYear, NormalMode)
  override def onwardRoute: Call            = routes.Class4NonDivingExemptController.onPageLoad(taxYear, NormalMode)
  override def validAnswer: ExemptionReason = TrusteeExecutorAdmin

  override def filledUserAnswers: UserAnswers = baseAnswers.set(page, validAnswer, nationalInsuranceContributions.some)(writes).success.value

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[Class4ExemptionReasonView]
    view(form, scenario.taxYear, scenario.userType, scenario.mode).toString()
  }
}
