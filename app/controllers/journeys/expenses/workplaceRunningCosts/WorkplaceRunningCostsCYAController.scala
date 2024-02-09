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

package controllers.journeys.expenses.workplaceRunningCosts

import controllers.actions._
import controllers.{handleSubmitAnswersResult, redirectJourneyRecovery}
import models.common._
import models.journeys.Journey.ExpensesWorkplaceRunningCosts
import models.journeys.expenses.workplaceRunningCosts.WorkplaceRunningCostsJourneyAnswers
import models.requests.DataRequest
import pages.expenses.workplaceRunningCosts.WorkplaceRunningCostsCYAPage
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.{BusinessPremisesAmountPage, LivingAtBusinessPremisesOnePerson, LivingAtBusinessPremisesThreePlusPeople, LivingAtBusinessPremisesTwoPeople}
import pages.expenses.workplaceRunningCosts.workingFromHome.{WorkingFromHomeHours101Plus, WorkingFromHomeHours25To50, WorkingFromHomeHours51To100}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.expenses.workplaceRunningCosts.{BusinessPremisesAmountSummary, BusinessPremisesDisallowableAmountSummary, LiveAtBusinessPremisesSummary, LivingAtBusinessPremisesOnePersonSummary, LivingAtBusinessPremisesThreePlusPeopleSummary, LivingAtBusinessPremisesTwoPeopleSummary, MoreThan25HoursSummary, WfbpClaimingAmountSummary, WfbpFlatRateOrActualCostsSummary, WfhClaimingAmountSummary, WfhFlatRateOrActualCostsSummary, WorkingFromHome101PlusHoursSummary, WorkingFromHome25To50HoursSummary, WorkingFromHome51To100HoursSummary}
import viewmodels.journeys.SummaryListCYA
import views.html.standard.CheckYourAnswersView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WorkplaceRunningCostsCYAController @Inject() (override val messagesApi: MessagesApi,
                                                    identify: IdentifierAction,
                                                    getUserAnswers: DataRetrievalAction,
                                                    getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                                    requireData: DataRequiredAction,
                                                    service: SelfEmploymentService,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: CheckYourAnswersView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getUserAnswers andThen getJourneyAnswers[WorkplaceRunningCostsJourneyAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, ExpensesWorkplaceRunningCosts)) andThen requireData) { implicit request =>
      val user = request.userType


      getFlatRates(request, businessId) match {
        case Left(_) => redirectJourneyRecovery()
        case Right(value) =>
          val wfhRate  = value._1.getOrElse("")
          val wfbpRate = value._2.getOrElse("")

          val summaryList = SummaryListCYA.summaryListOpt(
            List(
              MoreThan25HoursSummary.row(request.userAnswers, taxYear, businessId, user),
              WorkingFromHome25To50HoursSummary.row(request.userAnswers, taxYear, businessId, user),
              WorkingFromHome51To100HoursSummary.row(request.userAnswers, taxYear, businessId, user),
              WorkingFromHome101PlusHoursSummary.row(request.userAnswers, taxYear, businessId, user),
              WfhFlatRateOrActualCostsSummary.row(request.userAnswers, taxYear, businessId, user, wfhRate),
              WfhClaimingAmountSummary.row(request.userAnswers, taxYear, businessId, user),
              LiveAtBusinessPremisesSummary.row(request.userAnswers, taxYear, businessId, user),
              BusinessPremisesAmountSummary.row(request.userAnswers, taxYear, businessId, user),
              BusinessPremisesDisallowableAmountSummary.row(
                request.userAnswers,
                taxYear,
                businessId,
                user,
                request.userAnswers.get(BusinessPremisesAmountPage, Some(businessId)).get),
              LivingAtBusinessPremisesOnePersonSummary.row(request.userAnswers, taxYear, businessId, user),
              LivingAtBusinessPremisesTwoPeopleSummary.row(request.userAnswers, taxYear, businessId, user),
              LivingAtBusinessPremisesThreePlusPeopleSummary.row(request.userAnswers, taxYear, businessId, user),
              WfbpFlatRateOrActualCostsSummary.row(request.userAnswers, taxYear, businessId, user, wfbpRate),
              WfbpClaimingAmountSummary.row(request.userAnswers, taxYear, businessId, user)
            )
          )

          Ok(
            view(
              WorkplaceRunningCostsCYAPage.toString,
              taxYear,
              request.userType,
              summaryList,
              routes.WorkplaceRunningCostsCYAController.onSubmit(taxYear, businessId)))
      }
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = (identify andThen getUserAnswers andThen requireData).async {
    implicit request =>
      val context = JourneyContextWithNino(taxYear, request.nino, businessId, request.mtditid, ExpensesWorkplaceRunningCosts)
      val result  = service.submitAnswers[WorkplaceRunningCostsJourneyAnswers](context, request.userAnswers)

      handleSubmitAnswersResult(context, result)
  }

  def getFlatRates(request: DataRequest[_], businessId: BusinessId): Either[Unit, (Option[String], Option[String])] = {

    val months25To50  = request.getValue(WorkingFromHomeHours25To50, businessId)
    val months51To100 = request.getValue(WorkingFromHomeHours51To100, businessId)
    val months101Plus = request.getValue(WorkingFromHomeHours101Plus, businessId)

    val months1Person = request.getValue(LivingAtBusinessPremisesOnePerson, businessId)
    val months2People = request.getValue(LivingAtBusinessPremisesTwoPeople, businessId)
    val months3People = request.getValue(LivingAtBusinessPremisesThreePlusPeople, businessId)

    (months1Person, months2People, months3People, months25To50, months51To100, months101Plus) match {
      case (Some(months1Person), Some(months2People), Some(months3People), Some(months25To50), Some(months51To100), Some(months101Plus)) =>
        val amount25To50  = months25To50 * 10
        val amount51To100 = months51To100 * 18
        val amount101Plus = months101Plus * 26
        val wfhFlatRate   = amount25To50 + amount51To100 + amount101Plus

        val amount1Person = months1Person * 350
        val amount2People = months2People * 500
        val amount3People = months3People * 650
        val flatRate      = amount1Person + amount2People + amount3People
        Right((Some(formatMoney(wfhFlatRate)), Some(formatMoney(flatRate))))

      case _ => Left((): Unit)
    }
  }

}
