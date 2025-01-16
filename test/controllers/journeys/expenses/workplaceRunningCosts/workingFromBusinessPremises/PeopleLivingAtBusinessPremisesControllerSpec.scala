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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import base.questionPages.MultipleIntGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.PeopleLivingAtBusinessPremisesFormProvider
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.PeopleLivingAtBusinessPremisesFormProvider.PeopleLivingAtBusinessPremisesFormModel
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import play.api.Application
import play.api.data.Form
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import play.api.inject.{Binding, bind}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.POST
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.PeopleLivingAtBusinessPremisesView

class PeopleLivingAtBusinessPremisesControllerSpec
    extends MultipleIntGetAndPostQuestionBaseSpec[PeopleLivingAtBusinessPremisesFormModel](
      "PeopleLivingAtBusinessPremisesController",
      PeopleLivingAtBusinessPremisesPage) {

  override def onPageLoadRoute: String = routes.PeopleLivingAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode).url
  override def onSubmitRoute: String   = routes.PeopleLivingAtBusinessPremisesController.onSubmit(taxYear, businessId, NormalMode).url
  override def onwardRoute: Call       = routes.PeopleLivingAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)

  override def pageAnswers: UserAnswers = baseAnswers
    .set(LivingAtBusinessPremisesOnePerson, amount, businessId.some)
    .success
    .value
    .set(LivingAtBusinessPremisesTwoPeople, amount, businessId.some)
    .success
    .value
    .set(LivingAtBusinessPremisesThreePlusPeople, amount, businessId.some)
    .success
    .value

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())

  override val bindings: List[Binding[_]] = List(
    bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute))
  )

  private val maxMonths = 12

  override def createForm(userType: UserType): Form[PeopleLivingAtBusinessPremisesFormModel] =
    PeopleLivingAtBusinessPremisesFormProvider(userType, maxMonths)
  override def validFormModel: PeopleLivingAtBusinessPremisesFormModel = PeopleLivingAtBusinessPremisesFormModel(amount, amount, amount)

  override def postRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmitRoute).withFormUrlEncodedBody(
    ("onePerson", amount.toString),
    ("twoPeople", amount.toString),
    ("threePeople", amount.toString)
  )

  override def expectedView(expectedForm: Form[PeopleLivingAtBusinessPremisesFormModel], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[PeopleLivingAtBusinessPremisesView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, maxMonths.toString).toString()
  }

}
