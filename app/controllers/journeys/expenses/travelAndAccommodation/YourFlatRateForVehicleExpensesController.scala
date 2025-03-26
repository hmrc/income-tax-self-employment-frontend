/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.journeys.expenses.travelAndAccommodation

import controllers.actions._
import forms.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpenses
import models.requests.DataRequest
import navigation.TravelAndAccommodationNavigator
import pages.expenses.travelAndAccommodation.{SimplifiedExpensesPage, YourFlatRateForVehicleExpensesPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.checkAnswers.buildKeyValueRow
import views.html.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpensesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourFlatRateForVehicleExpensesController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: YourFlatRateForVehicleExpensesFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: YourFlatRateForVehicleExpensesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[YourFlatRateForVehicleExpenses] = formProvider()

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(YourFlatRateForVehicleExpensesPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      loadPage(preparedForm, Ok, taxYear, businessId, mode)

  }

  private def loadPage(form: Form[_], status: Status, taxYear: TaxYear, businessId: BusinessId, mode: Mode)(implicit
      request: DataRequest[AnyContent]): Result = {
    val workMileage              = BigDecimal(90.00)
    val summaryList: SummaryList = buildSummaryList(workMileage)
    val showSelection            = request.userAnswers.get(SimplifiedExpensesPage, businessId).contains(false)
    status(view(form, taxYear, businessId, request.userType, workMileage.toString(), summaryList, showSelection, mode))
  }

  private def buildSummaryList(workMileage: BigDecimal)(implicit request: DataRequest[AnyContent]): SummaryList = {
    val overTheLimitPrice: Double = 0.25
    val limitPrice: Double        = 0.45
    val mileageLimit: Int         = 10000

    def standardLimitRow(mileage: BigDecimal, limit: BigDecimal) = buildKeyValueRow(
      s"yourFlatRateForVehicleExpenses.c1.45p",
      s"yourFlatRateForVehicleExpenses.c2.45p",
      optKeyArgs = Seq(stripTrailingZeros(mileage)),
      optValueArgs = Seq(limit.toString())
    )

    val rows = if (workMileage > mileageLimit) {
      val aboveMileage     = workMileage - mileageLimit
      val aboveLimitAmount = aboveMileage * overTheLimitPrice
      val limitAmount      = mileageLimit * limitPrice

      def aboveLimitRow(aboveMileage: BigDecimal, aboveLimit: BigDecimal) = buildKeyValueRow(
        s"yourFlatRateForVehicleExpenses.c1.25p",
        s"yourFlatRateForVehicleExpenses.c2.25p",
        optKeyArgs = Seq(stripTrailingZeros(aboveMileage)),
        optValueArgs = Seq(aboveLimit.toString())
      )

      Seq(standardLimitRow(mileageLimit, limitAmount), aboveLimitRow(aboveMileage, aboveLimitAmount))
    } else {
      val limit = workMileage * limitPrice
      Seq(standardLimitRow(workMileage, limit))
    }

    SummaryList(rows).copy(classes = "govuk-summary-list--half")
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(SimplifiedExpensesPage, businessId) match {
        case Some(boolean) if boolean =>
          Future.successful(Redirect(navigator.nextPage(YourFlatRateForVehicleExpensesPage, mode, request.userAnswers, taxYear, businessId)))
        case Some(boolean) if !boolean =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(loadPage(formWithErrors, BadRequest, taxYear, businessId, mode)),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(YourFlatRateForVehicleExpensesPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(YourFlatRateForVehicleExpensesPage, mode, updatedAnswers, taxYear, businessId))
            )
        case None => Future.successful(Redirect(controllers.standard.routes.JourneyRecoveryController.onPageLoad()))
      }

  }
}
