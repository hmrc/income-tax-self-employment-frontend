/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.prepop

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import models.common.Journey.IncomePrepop
import models.common._
import models.journeys.income.IncomePrepopAnswers
import pages.prepop.{IncomeOtherAmount, IncomeTurnoverAmount}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.buildTable
import viewmodels.checkAnswers.prepop.PrepopIncomeSummary.{headRow, otherIncomeRow, totalIncomeRow, turnoverIncomeRow}
import views.html.journeys.prepop.BusinessIncomeSummaryView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessIncomeSummaryController @Inject() (override val messagesApi: MessagesApi,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                                 requireData: DataRequiredAction,
                                                 view: BusinessIncomeSummaryView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getData andThen getJourneyAnswers[IncomePrepopAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, IncomePrepop)) andThen requireData) { implicit request =>
      val answers = IncomePrepopAnswers(
        request.getValue(IncomeTurnoverAmount, businessId),
        request.getValue(IncomeOtherAmount, businessId)
      )
      val incomeTable = buildTable(
        headRow,
        Seq(
          turnoverIncomeRow(answers),
          otherIncomeRow(answers),
          totalIncomeRow(answers)
        ).flatten)
      Ok(view(request.userType, taxYear, businessId, incomeTable))
    }

}
